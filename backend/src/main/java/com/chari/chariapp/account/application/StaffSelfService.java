package com.chari.chariapp.account.application;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.PasswordHasher;
import com.chari.chariapp.account.application.port.out.RefreshSessionStore;
import com.chari.chariapp.account.application.port.out.StaffProfileStore;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.StaffProfile;
import com.chari.chariapp.citizen.domain.PhoneReference;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class StaffSelfService {
    private final AccountStore accounts;
    private final StaffProfileStore profiles;
    private final PasswordHasher passwordHasher;
    private final RefreshSessionStore sessions;
    private final OperationalAuditStore audit;
    private final Clock clock;

    public StaffSelfService(AccountStore accounts, StaffProfileStore profiles, PasswordHasher passwordHasher,
                            RefreshSessionStore sessions, OperationalAuditStore audit, Clock clock) {
        this.accounts = accounts; this.profiles = profiles; this.passwordHasher = passwordHasher;
        this.sessions = sessions; this.audit = audit; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public StaffAccountView get(AccountId accountId) {
        Account account = requireStaff(accountId);
        return new StaffAccountView(account, profiles.findByAccountId(accountId).orElse(null));
    }

    @Transactional
    public StaffProfile update(AccountId accountId, String firstName, String lastName, PhoneReference phone) {
        requireStaff(accountId);
        Instant now = Instant.now(clock);
        StaffProfile current = profiles.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Staff profile not found"));
        StaffProfile saved = profiles.save(current.update(firstName, lastName, phone, current.jobTitle(), now));
        audit.record(accountId.value().toString(), "STAFF_PROFILE_UPDATED", "ACCOUNT", accountId.value().toString(), null, now);
        return saved;
    }

    @Transactional
    public void changePassword(AccountId accountId, String currentPassword, String newPassword) {
        Account account = requireStaff(accountId);
        if (!passwordHasher.matches(currentPassword, account.passwordHash())) {
            throw new IllegalArgumentException("Current password is invalid");
        }
        if (newPassword == null || newPassword.length() < 12 || newPassword.length() > 128) throw new WeakPasswordException();
        Instant now = Instant.now(clock);
        accounts.updatePasswordAndInvalidateAuthorization(accountId, passwordHasher.hash(newPassword));
        sessions.revokeAllForAccount(accountId, now);
        audit.record(accountId.value().toString(), "STAFF_PASSWORD_CHANGED", "ACCOUNT", accountId.value().toString(), null, now);
    }

    @Transactional
    public void logoutAll(AccountId accountId) {
        requireStaff(accountId);
        Instant now = Instant.now(clock);
        accounts.invalidateAuthorization(accountId);
        sessions.revokeAllForAccount(accountId, now);
        audit.record(accountId.value().toString(), "STAFF_LOGOUT_ALL", "ACCOUNT", accountId.value().toString(), null, now);
    }

    private Account requireStaff(AccountId id) {
        Account account = accounts.findById(id).orElseThrow(() -> new IllegalArgumentException("Staff account not found"));
        if (account.status() != AccountStatus.ACTIVE || account.roles().stream()
                .noneMatch(role -> role == AccountRole.ADMIN || role == AccountRole.EMPLOYEE)) {
            throw new IllegalArgumentException("Staff account not found");
        }
        return account;
    }
}
