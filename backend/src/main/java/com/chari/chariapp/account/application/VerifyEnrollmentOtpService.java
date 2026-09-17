package com.chari.chariapp.account.application;

import com.chari.chariapp.account.application.port.out.EnrollmentChallengeStore;
import com.chari.chariapp.account.application.port.out.VerificationCodeHasher;
import com.chari.chariapp.account.domain.EnrollmentChallenge;
import com.chari.chariapp.account.domain.VerificationChallengePurpose;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.PhoneReference;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class VerifyEnrollmentOtpService implements VerifyEnrollmentOtpUseCase {

    private final EnrollmentChallengeStore challengeStore;
    private final CitizenStore citizenStore;
    private final VerificationCodeHasher codeHasher;
    private final Clock clock;

    public VerifyEnrollmentOtpService(
            EnrollmentChallengeStore challengeStore,
            CitizenStore citizenStore,
            VerificationCodeHasher codeHasher,
            Clock clock
    ) {
        this.challengeStore = Objects.requireNonNull(challengeStore, "Challenge store is required");
        this.citizenStore = Objects.requireNonNull(citizenStore, "Citizen store is required");
        this.codeHasher = Objects.requireNonNull(codeHasher, "Verification code hasher is required");
        this.clock = Objects.requireNonNull(clock, "Clock is required");
    }

    @Override
    @Transactional
    public void verify(VerifyEnrollmentOtpCommand command) {
        Objects.requireNonNull(command, "Verify OTP command is required");
        EnrollmentChallenge challenge = challengeStore.findById(command.challengeId())
                .orElseThrow(InvalidEnrollmentOtpException::new);
        Instant now = Instant.now(clock);

        if (challenge.isExpired(now) || challenge.isLocked() || challenge.isVerified()) {
            throw new InvalidEnrollmentOtpException();
        }
        if (!codeHasher.matches(command.code(), challenge.codeHash())) {
            challengeStore.save(challenge.registerFailedAttempt());
            throw new InvalidEnrollmentOtpException();
        }
        if (challenge.purpose() == VerificationChallengePurpose.ACCOUNT_ENROLLMENT) {
            citizenStore.updateVerifiedPhone(
                    challenge.citizenId(),
                    new PhoneReference(
                            challenge.destinationLookup(),
                            challenge.destinationCiphertextOptional().orElseThrow()
                    ),
                    now
            );
        }
        challengeStore.save(challenge.markVerified(now));
    }
}
