package com.chari.chariapp.citizen.application;

import com.chari.chariapp.account.application.VerifyEnrollmentOtpService;
import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.EnrollmentChallengeStore;
import com.chari.chariapp.account.application.port.out.OtpCodeGenerator;
import com.chari.chariapp.account.application.port.out.OtpSender;
import com.chari.chariapp.account.application.port.out.VerificationCodeHasher;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.EmailReference;
import com.chari.chariapp.account.domain.EnrollmentChallenge;
import com.chari.chariapp.account.domain.EnrollmentChallengeId;
import com.chari.chariapp.account.domain.VerificationChallengePurpose;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import com.chari.chariapp.citizen.domain.PhoneReference;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CitizenPhoneVerificationServicesTests {
    private static final Instant NOW = Instant.parse("2026-08-28T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final String PHONE_LOOKUP = "b".repeat(64);

    @Test
    void verifiesThePhoneBeforeAssigningItAndRecordsTheOperatorAuditTrail() {
        Citizen citizen = new Citizen(CitizenId.newId(), new NationalIdReference("a".repeat(64), "encrypted-national-id"),
                null, null, NOW.minusSeconds(60));
        MutableCitizenStore citizens = new MutableCitizenStore(citizen);
        Account operator = new Account(AccountId.newId(), null, new EmailReference("c".repeat(64), "encrypted-email"),
                "password-hash", AccountStatus.ACTIVE, Set.of(AccountRole.EMPLOYEE), NOW.minusSeconds(60));
        MutableChallengeStore challenges = new MutableChallengeStore();
        CapturingOtpSender sender = new CapturingOtpSender();
        CapturingAuditStore audit = new CapturingAuditStore();
        VerificationCodeHasher hasher = new PrefixHasher();

        RequestCitizenPhoneVerificationService requestService = new RequestCitizenPhoneVerificationService(
                citizens, new SingleAccountStore(operator), challenges, () -> "123456", hasher, sender, audit, CLOCK
        );
        EnrollmentChallengeId challengeId = requestService.request(new RequestCitizenPhoneVerificationCommand(
                citizen.id(), new PhoneReference(PHONE_LOOKUP, "encrypted-phone"), operator.id()
        ));

        EnrollmentChallenge pending = challenges.findById(challengeId).orElseThrow();
        assertThat(pending.purpose()).isEqualTo(VerificationChallengePurpose.CITIZEN_PHONE_VERIFICATION);
        assertThat(pending.codeHash()).isEqualTo("hash:123456");
        assertThat(sender.destination.lookup()).isEqualTo(PHONE_LOOKUP);

        ConfirmCitizenPhoneVerificationService confirmService = new ConfirmCitizenPhoneVerificationService(
                citizens, new SingleAccountStore(operator), challenges,
                new VerifyEnrollmentOtpService(challenges, citizens, hasher, CLOCK), audit, CLOCK
        );
        confirmService.confirm(new ConfirmCitizenPhoneVerificationCommand(citizen.id(), challengeId, "123456", operator.id()));

        assertThat(citizens.findById(citizen.id()).orElseThrow().verifiedPhoneOptional())
                .contains(new PhoneReference(PHONE_LOOKUP, "encrypted-phone"));
        assertThat(challenges.findById(challengeId).orElseThrow().isEnrollmentConsumed()).isTrue();
        assertThat(audit.actions).containsExactly("CITIZEN_PHONE_VERIFICATION_REQUESTED", "CITIZEN_PHONE_VERIFICATION_CONFIRMED");
    }

    private static final class PrefixHasher implements VerificationCodeHasher {
        public String hash(String code) { return "hash:" + code; }
        public boolean matches(String code, String hash) { return hash(code).equals(hash); }
    }

    private static final class CapturingOtpSender implements OtpSender {
        private PhoneReference destination;
        public void sendEnrollmentCode(PhoneReference destination, String code) { this.destination = destination; }
    }

    private static final class CapturingAuditStore implements OperationalAuditStore {
        private final ArrayList<String> actions = new ArrayList<>();
        public void record(String actorAccountId, String action, String targetType, String targetId, String metadata, Instant occurredAt) {
            actions.add(action);
        }
    }

    private static final class MutableChallengeStore implements EnrollmentChallengeStore {
        private final Map<EnrollmentChallengeId, EnrollmentChallenge> challenges = new HashMap<>();
        public EnrollmentChallenge save(EnrollmentChallenge challenge) { challenges.put(challenge.id(), challenge); return challenge; }
        public Optional<EnrollmentChallenge> findById(EnrollmentChallengeId id) { return Optional.ofNullable(challenges.get(id)); }
        public boolean consumeVerified(EnrollmentChallengeId id, Instant consumedAt) {
            EnrollmentChallenge challenge = challenges.get(id);
            if (challenge == null || !challenge.isVerified() || challenge.isExpired(consumedAt) || challenge.isEnrollmentConsumed()) return false;
            challenges.put(id, challenge.markEnrollmentConsumed(consumedAt));
            return true;
        }
    }

    private static final class MutableCitizenStore implements CitizenStore {
        private Citizen citizen;
        private MutableCitizenStore(Citizen citizen) { this.citizen = citizen; }
        public boolean existsByNationalIdLookup(String lookup) { return citizen.nationalId().lookup().equals(lookup); }
        public Optional<Citizen> findByNationalIdLookup(String lookup) { return existsByNationalIdLookup(lookup) ? Optional.of(citizen) : Optional.empty(); }
        public Optional<Citizen> findById(CitizenId id) { return citizen.id().equals(id) ? Optional.of(citizen) : Optional.empty(); }
        public Citizen updateVerifiedPhone(CitizenId id, PhoneReference phone, Instant verifiedAt) {
            if (!citizen.id().equals(id)) throw new IllegalArgumentException();
            citizen = new Citizen(citizen.id(), citizen.nationalId(), phone, verifiedAt, citizen.createdAt());
            return citizen;
        }
        public Citizen save(Citizen citizen) { this.citizen = citizen; return citizen; }
    }

    private record SingleAccountStore(Account account) implements AccountStore {
        public boolean existsByEmailLookup(String emailLookup) { return false; }
        public boolean existsByCitizenId(CitizenId citizenId) { return false; }
        public Optional<Account> findByEmailLookup(String emailLookup) { return Optional.empty(); }
        public Optional<Account> findById(AccountId accountId) { return account.id().equals(accountId) ? Optional.of(account) : Optional.empty(); }
        public Account save(Account account) { return account; }
    }
}
