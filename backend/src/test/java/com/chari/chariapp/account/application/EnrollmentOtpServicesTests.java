package com.chari.chariapp.account.application;

import com.chari.chariapp.account.application.port.out.EnrollmentChallengeStore;
import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.OtpCodeGenerator;
import com.chari.chariapp.account.application.port.out.OtpSender;
import com.chari.chariapp.account.application.port.out.VerificationCodeHasher;
import com.chari.chariapp.account.domain.EnrollmentChallenge;
import com.chari.chariapp.account.domain.EnrollmentChallengeId;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.VerificationChallengePurpose;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import com.chari.chariapp.citizen.domain.PhoneReference;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnrollmentOtpServicesTests {

    private static final String NATIONAL_ID_LOOKUP = "a".repeat(64);
    private static final String PHONE_LOOKUP = "b".repeat(64);
    private static final String EMAIL_LOOKUP = "c".repeat(64);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void sendsOtpToTheCitizensVerifiedPhoneAndStoresOnlyItsHash() {
        Citizen citizen = citizen();
        InMemoryChallengeStore challengeStore = new InMemoryChallengeStore();
        CapturingOtpSender sender = new CapturingOtpSender();
        RequestEnrollmentOtpService service = new RequestEnrollmentOtpService(
                new SingleCitizenStore(citizen), new NoAccountsStore(), challengeStore,
                () -> "123456", new PrefixCodeHasher(), sender, CLOCK
        );

        EnrollmentChallengeId challengeId = service.request(new RequestEnrollmentOtpCommand(
                NATIONAL_ID_LOOKUP, citizen.verifiedPhone(), EMAIL_LOOKUP
        ));

        EnrollmentChallenge challenge = challengeStore.findById(challengeId).orElseThrow();
        assertThat(challenge.destinationLookup()).isEqualTo(PHONE_LOOKUP);
        assertThat(challenge.codeHash()).isEqualTo("hash:123456");
        assertThat(challenge.codeHash()).isNotEqualTo("123456");
        assertThat(sender.destination).isEqualTo(citizen.verifiedPhone());
        assertThat(sender.code).isEqualTo("123456");
    }

    @Test
    void sendsOtpToThePhoneProvidedForAMigratedCitizen() {
        Citizen migratedCitizen = new Citizen(
                CitizenId.newId(),
                new NationalIdReference(NATIONAL_ID_LOOKUP, "encrypted-national-id"),
                null,
                null,
                Instant.parse("2026-08-27T00:00:00Z")
        );
        RequestEnrollmentOtpService service = new RequestEnrollmentOtpService(
                new SingleCitizenStore(migratedCitizen), new NoAccountsStore(), new InMemoryChallengeStore(), () -> "123456",
                new PrefixCodeHasher(), new CapturingOtpSender(), CLOCK
        );

        EnrollmentChallengeId challengeId = service.request(new RequestEnrollmentOtpCommand(
                NATIONAL_ID_LOOKUP, new PhoneReference(PHONE_LOOKUP, "encrypted-phone"), EMAIL_LOOKUP
        ));

        assertThat(challengeId).isNotNull();
    }

    @Test
    void verifiesTheCorrectOtpAndRecordsTheVerificationTime() {
        InMemoryChallengeStore store = new InMemoryChallengeStore();
        Citizen citizen = citizen();
        SingleCitizenStore citizens = new SingleCitizenStore(citizen);
        EnrollmentChallenge challenge = new EnrollmentChallenge(
                EnrollmentChallengeId.newId(), citizen.id(), VerificationChallengePurpose.ACCOUNT_ENROLLMENT,
                PHONE_LOOKUP, "encrypted-phone", "hash:123456",
                Instant.parse("2026-08-28T00:05:00Z"), 0, null, null
        );
        store.save(challenge);
        VerifyEnrollmentOtpService service = new VerifyEnrollmentOtpService(
                store, citizens, new PrefixCodeHasher(), CLOCK
        );

        service.verify(new VerifyEnrollmentOtpCommand(challenge.id(), "123456"));

        assertThat(store.findById(challenge.id()).orElseThrow().verifiedAt())
                .isEqualTo(Instant.parse("2026-08-28T00:00:00Z"));
        assertThat(citizens.findById(citizen.id()).orElseThrow().verifiedPhoneOptional())
                .contains(new PhoneReference(PHONE_LOOKUP, "encrypted-phone"));
    }

    @Test
    void countsIncorrectOtpAttemptsWithoutExposingTheCorrectCode() {
        InMemoryChallengeStore store = new InMemoryChallengeStore();
        Citizen citizen = citizen();
        EnrollmentChallenge challenge = new EnrollmentChallenge(
                EnrollmentChallengeId.newId(), citizen.id(), VerificationChallengePurpose.ACCOUNT_ENROLLMENT,
                PHONE_LOOKUP, "encrypted-phone", "hash:123456",
                Instant.parse("2026-08-28T00:05:00Z"), 0, null, null
        );
        store.save(challenge);
        VerifyEnrollmentOtpService service = new VerifyEnrollmentOtpService(
                store, new SingleCitizenStore(citizen), new PrefixCodeHasher(), CLOCK
        );

        assertThatThrownBy(() -> service.verify(new VerifyEnrollmentOtpCommand(challenge.id(), "000000")))
                .isInstanceOf(InvalidEnrollmentOtpException.class);

        assertThat(store.findById(challenge.id()).orElseThrow().failedAttempts()).isEqualTo(1);
    }

    private static Citizen citizen() {
        return new Citizen(
                CitizenId.newId(),
                new NationalIdReference(NATIONAL_ID_LOOKUP, "encrypted-national-id"),
                new PhoneReference(PHONE_LOOKUP, "encrypted-phone"),
                Instant.parse("2026-08-27T00:00:00Z"),
                Instant.parse("2026-08-27T00:00:00Z")
        );
    }

    private static final class PrefixCodeHasher implements VerificationCodeHasher {
        @Override
        public String hash(String code) {
            return "hash:" + code;
        }

        @Override
        public boolean matches(String code, String hash) {
            return hash(code).equals(hash);
        }
    }

    private static final class CapturingOtpSender implements OtpSender {
        private PhoneReference destination;
        private String code;

        @Override
        public void sendEnrollmentCode(PhoneReference destination, String code) {
            this.destination = destination;
            this.code = code;
        }
    }

    private static final class InMemoryChallengeStore implements EnrollmentChallengeStore {
        private final Map<EnrollmentChallengeId, EnrollmentChallenge> challenges = new HashMap<>();

        @Override
        public EnrollmentChallenge save(EnrollmentChallenge challenge) {
            challenges.put(challenge.id(), challenge);
            return challenge;
        }

        @Override
        public Optional<EnrollmentChallenge> findById(EnrollmentChallengeId id) {
            return Optional.ofNullable(challenges.get(id));
        }

        @Override
        public boolean consumeVerified(EnrollmentChallengeId id, Instant consumedAt) {
            EnrollmentChallenge challenge = challenges.get(id);
            if (challenge == null || !challenge.isVerified() || challenge.isExpired(consumedAt)
                    || challenge.isEnrollmentConsumed()) {
                return false;
            }
            challenges.put(id, challenge.markEnrollmentConsumed(consumedAt));
            return true;
        }
    }

    private static final class SingleCitizenStore implements CitizenStore {
        private Citizen citizen;

        private SingleCitizenStore(Citizen citizen) {
            this.citizen = citizen;
        }

        @Override
        public boolean existsByNationalIdLookup(String nationalIdLookup) {
            return NATIONAL_ID_LOOKUP.equals(nationalIdLookup);
        }

        @Override
        public Optional<Citizen> findByNationalIdLookup(String nationalIdLookup) {
            return NATIONAL_ID_LOOKUP.equals(nationalIdLookup) ? Optional.of(citizen) : Optional.empty();
        }

        @Override
        public Optional<Citizen> findById(CitizenId citizenId) {
            return citizen.id().equals(citizenId) ? Optional.of(citizen) : Optional.empty();
        }

        @Override
        public Citizen updateVerifiedPhone(CitizenId citizenId, PhoneReference verifiedPhone, Instant verifiedAt) {
            citizen = new Citizen(citizen.id(), citizen.nationalId(), verifiedPhone, verifiedAt, citizen.createdAt());
            return citizen;
        }

        @Override
        public Citizen save(Citizen citizen) {
            return citizen;
        }
    }

    private static class NoAccountsStore implements AccountStore {
        @Override
        public boolean existsByEmailLookup(String emailLookup) {
            return false;
        }

        @Override
        public boolean existsByCitizenId(CitizenId citizenId) {
            return false;
        }

        @Override
        public Optional<Account> findByEmailLookup(String emailLookup) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findById(AccountId accountId) {
            return Optional.empty();
        }

        @Override
        public Account save(Account account) {
            return account;
        }
    }
}
