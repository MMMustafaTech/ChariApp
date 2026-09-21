package com.chari.chariapp.account.application;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.PasswordHasher;
import com.chari.chariapp.account.application.port.out.RefreshSessionStore;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.EmailReference;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import com.chari.chariapp.citizen.domain.PhoneReference;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CitizenProfileServicesTests {

    private static final Instant NOW = Instant.parse("2026-08-29T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void returnsOnlyMaskedRegistryIdentifiersInProfile() {
        Account account = citizenAccount();
        Citizen citizen = citizen(account.citizenId());
        AccountStore accounts = mock(AccountStore.class);
        CitizenStore citizens = mock(CitizenStore.class);
        PersonalDataProtector protector = mock(PersonalDataProtector.class);
        when(accounts.findById(account.id())).thenReturn(Optional.of(account));
        when(citizens.findById(account.citizenId())).thenReturn(Optional.of(citizen));
        when(protector.decrypt("email-ciphertext")).thenReturn("citizen@example.test");
        when(protector.decrypt("national-ciphertext")).thenReturn("CID002");
        when(protector.decrypt("phone-ciphertext")).thenReturn("+23590000001");

        CitizenProfile profile = new CitizenProfileService(accounts, citizens, protector).profile(account.id());

        assertThat(profile.email()).isEqualTo("citizen@example.test");
        assertThat(profile.maskedNationalId()).isEqualTo("****02");
        assertThat(profile.maskedVerifiedPhone()).isEqualTo("*********001");
        assertThat(profile.phoneVerified()).isTrue();
    }

    @Test
    void changingPasswordInvalidatesTokensAndRevokesAllRefreshSessions() {
        Account account = citizenAccount();
        AccountStore accounts = mock(AccountStore.class);
        PasswordHasher passwords = mock(PasswordHasher.class);
        RefreshSessionStore sessions = mock(RefreshSessionStore.class);
        OperationalAuditStore audit = mock(OperationalAuditStore.class);
        when(accounts.findById(account.id())).thenReturn(Optional.of(account));
        when(passwords.matches("CurrentPass123!", account.passwordHash())).thenReturn(true);
        when(passwords.matches("NewSecurePass123!", account.passwordHash())).thenReturn(false);
        when(passwords.hash("NewSecurePass123!")).thenReturn("new-password-hash");

        new CitizenSecurityService(accounts, passwords, sessions, audit, CLOCK)
                .changePassword(account.id(), "CurrentPass123!", "NewSecurePass123!");

        verify(accounts).updatePasswordAndInvalidateAuthorization(account.id(), "new-password-hash");
        verify(sessions).revokeAllForAccount(account.id(), NOW);
    }

    @Test
    void logoutAllDevicesInvalidatesAccessTokensAndRefreshSessions() {
        Account account = citizenAccount();
        AccountStore accounts = mock(AccountStore.class);
        RefreshSessionStore sessions = mock(RefreshSessionStore.class);
        when(accounts.findById(account.id())).thenReturn(Optional.of(account));
        when(sessions.revokeAllForAccount(account.id(), NOW)).thenReturn(2);

        int revoked = new CitizenSecurityService(
                accounts, mock(PasswordHasher.class), sessions, mock(OperationalAuditStore.class), CLOCK
        ).logoutAllDevices(account.id());

        assertThat(revoked).isEqualTo(2);
        verify(accounts).invalidateAuthorization(account.id());
        verify(sessions).revokeAllForAccount(account.id(), NOW);
    }

    private static Account citizenAccount() {
        return new Account(
                AccountId.newId(), CitizenId.newId(),
                new EmailReference("a".repeat(64), "email-ciphertext"),
                "old-password-hash", AccountStatus.ACTIVE, Set.of(AccountRole.CITIZEN), NOW.minusSeconds(60)
        );
    }

    private static Citizen citizen(CitizenId citizenId) {
        return new Citizen(
                citizenId,
                new NationalIdReference("b".repeat(64), "national-ciphertext"),
                new PhoneReference("c".repeat(64), "phone-ciphertext"),
                NOW.minusSeconds(120),
                NOW.minusSeconds(300)
        );
    }
}
