package com.chari.chariapp.account.application;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.EnrollmentChallengeStore;
import com.chari.chariapp.account.application.port.out.PasswordHasher;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.EmailReference;
import com.chari.chariapp.account.domain.EnrollmentChallenge;
import com.chari.chariapp.account.domain.VerificationChallengePurpose;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/** Creates a citizen account only when its SMS enrollment proof is valid and unused. */
public class CreateVerifiedCitizenAccountService implements CreateVerifiedCitizenAccountUseCase {

    private final AccountStore accountStore;
    private final EnrollmentChallengeStore challengeStore;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    public CreateVerifiedCitizenAccountService(
            AccountStore accountStore,
            EnrollmentChallengeStore challengeStore,
            PasswordHasher passwordHasher,
            Clock clock
    ) {
        this.accountStore = Objects.requireNonNull(accountStore, "Account store is required");
        this.challengeStore = Objects.requireNonNull(challengeStore, "Challenge store is required");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "Password hasher is required");
        this.clock = Objects.requireNonNull(clock, "Clock is required");
    }

    @Override
    @Transactional
    public AccountId create(CreateVerifiedCitizenAccountCommand command) {
        Objects.requireNonNull(command, "Create verified citizen account command is required");
        validatePassword(command.rawPassword());

        EnrollmentChallenge challenge = challengeStore.findById(command.enrollmentChallengeId())
                .orElseThrow(EnrollmentProofUnavailableException::new);
        Instant now = Instant.now(clock);
        if (challenge.purpose() != VerificationChallengePurpose.ACCOUNT_ENROLLMENT
                || !challenge.isVerified() || challenge.isExpired(now) || challenge.isEnrollmentConsumed()) {
            throw new EnrollmentProofUnavailableException();
        }

        EmailReference email = new EmailReference(command.emailLookup(), command.encryptedEmail());
        if (accountStore.existsByCitizenId(challenge.citizenId())) {
            throw new EnrollmentUnavailableException(
                    EnrollmentUnavailableException.Reason.CITIZEN_ACCOUNT_EXISTS
            );
        }
        if (accountStore.existsByEmailLookup(email.lookup())) {
            throw new EnrollmentUnavailableException(
                    EnrollmentUnavailableException.Reason.EMAIL_ALREADY_EXISTS
            );
        }

        // The conditional update prevents two simultaneous registration requests from using the same proof.
        if (!challengeStore.consumeVerified(challenge.id(), now)) {
            throw new EnrollmentProofUnavailableException();
        }

        Account account = new Account(
                AccountId.newId(),
                challenge.citizenId(),
                email,
                passwordHasher.hash(command.rawPassword()),
                AccountStatus.ACTIVE,
                Set.of(AccountRole.CITIZEN),
                now
        );
        return accountStore.save(account).id();
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12 || password.length() > 128) {
            throw new WeakPasswordException();
        }
    }
}
