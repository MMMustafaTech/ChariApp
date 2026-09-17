package com.chari.chariapp.account.application;

import com.chari.chariapp.account.application.port.out.EnrollmentChallengeStore;
import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.OtpCodeGenerator;
import com.chari.chariapp.account.application.port.out.OtpSender;
import com.chari.chariapp.account.application.port.out.VerificationCodeHasher;
import com.chari.chariapp.account.domain.EnrollmentChallenge;
import com.chari.chariapp.account.domain.EnrollmentChallengeId;
import com.chari.chariapp.account.domain.VerificationChallengePurpose;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.PhoneReference;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class RequestEnrollmentOtpService implements RequestEnrollmentOtpUseCase {

    private static final Duration OTP_LIFETIME = Duration.ofMinutes(5);

    private final CitizenStore citizenStore;
    private final AccountStore accountStore;
    private final EnrollmentChallengeStore challengeStore;
    private final OtpCodeGenerator codeGenerator;
    private final VerificationCodeHasher codeHasher;
    private final OtpSender otpSender;
    private final Clock clock;

    public RequestEnrollmentOtpService(
            CitizenStore citizenStore,
            AccountStore accountStore,
            EnrollmentChallengeStore challengeStore,
            OtpCodeGenerator codeGenerator,
            VerificationCodeHasher codeHasher,
            OtpSender otpSender,
            Clock clock
    ) {
        this.citizenStore = Objects.requireNonNull(citizenStore, "Citizen store is required");
        this.accountStore = Objects.requireNonNull(accountStore, "Account store is required");
        this.challengeStore = Objects.requireNonNull(challengeStore, "Challenge store is required");
        this.codeGenerator = Objects.requireNonNull(codeGenerator, "OTP code generator is required");
        this.codeHasher = Objects.requireNonNull(codeHasher, "Verification code hasher is required");
        this.otpSender = Objects.requireNonNull(otpSender, "OTP sender is required");
        this.clock = Objects.requireNonNull(clock, "Clock is required");
    }

    @Override
    public EnrollmentChallengeId request(RequestEnrollmentOtpCommand command) {
        Objects.requireNonNull(command, "Request OTP command is required");
        Citizen citizen = citizenStore.findByNationalIdLookup(command.nationalIdLookup())
                .orElseThrow(() -> new EnrollmentUnavailableException(
                        EnrollmentUnavailableException.Reason.NATIONAL_ID_NOT_FOUND
                ));
        if (accountStore.existsByCitizenId(citizen.id())) {
            throw new EnrollmentUnavailableException(
                    EnrollmentUnavailableException.Reason.CITIZEN_ACCOUNT_EXISTS
            );
        }
        if (accountStore.existsByEmailLookup(command.emailLookup())) {
            throw new EnrollmentUnavailableException(
                    EnrollmentUnavailableException.Reason.EMAIL_ALREADY_EXISTS
            );
        }
        PhoneReference requestedPhone = Objects.requireNonNull(command.phone(), "Phone is required");

        String code = codeGenerator.generate();
        Instant now = Instant.now(clock);
        EnrollmentChallenge challenge = new EnrollmentChallenge(
                EnrollmentChallengeId.newId(),
                citizen.id(),
                VerificationChallengePurpose.ACCOUNT_ENROLLMENT,
                requestedPhone.lookup(),
                requestedPhone.ciphertext(),
                codeHasher.hash(code),
                now.plus(OTP_LIFETIME),
                0,
                null,
                null
        );
        challengeStore.save(challenge);
        otpSender.sendEnrollmentCode(requestedPhone, code);
        return challenge.id();
    }
}
