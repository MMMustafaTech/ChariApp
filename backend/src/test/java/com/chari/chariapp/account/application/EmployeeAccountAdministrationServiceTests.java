package com.chari.chariapp.account.application;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.PasswordHasher;
import com.chari.chariapp.account.application.port.out.RefreshSessionStore;
import com.chari.chariapp.account.application.port.out.StaffProfileStore;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.EmailReference;
import com.chari.chariapp.account.domain.RefreshSession;
import com.chari.chariapp.account.domain.StaffProfile;
import com.chari.chariapp.citizen.domain.CitizenId;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeAccountAdministrationServiceTests {
    private static final Instant NOW = Instant.parse("2026-08-28T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void activeAdministratorCanProvisionAndDisableAnEmployeeWithoutGrantingAdmin() {
        MemoryAccountStore accounts = new MemoryAccountStore();
        Account administrator = account(Set.of(AccountRole.ADMIN), AccountStatus.ACTIVE);
        accounts.save(administrator);
        CapturingAuditStore audit = new CapturingAuditStore();
        EmployeeAccountAdministrationService service = service(accounts, audit);

        AccountId employeeId = service.provisionEmployee(new ProvisionEmployeeAccountCommand(
                administrator.id(), "a".repeat(64), "encrypted-employee-email", "a-secure-password"
        ));

        Account employee = accounts.findById(employeeId).orElseThrow();
        assertThat(employee.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(employee.roles()).containsExactly(AccountRole.EMPLOYEE);

        service.changeEmployeeStatus(new ChangeEmployeeAccountStatusCommand(
                administrator.id(), employeeId, AccountStatus.DISABLED
        ));
        assertThat(accounts.findById(employeeId).orElseThrow().status()).isEqualTo(AccountStatus.DISABLED);
        assertThat(audit.actions).containsExactly("EMPLOYEE_ACCOUNT_PROVISIONED", "EMPLOYEE_ACCOUNT_STATUS_CHANGED");
    }

    @Test
    void employeeCannotUseTheAdministrationServiceAsAnAdministrator() {
        MemoryAccountStore accounts = new MemoryAccountStore();
        Account employee = account(Set.of(AccountRole.EMPLOYEE), AccountStatus.ACTIVE);
        accounts.save(employee);

        assertThatThrownBy(() -> service(accounts, new CapturingAuditStore()).provisionEmployee(
                new ProvisionEmployeeAccountCommand(employee.id(), "a".repeat(64), "encrypted-email", "a-secure-password")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    private static EmployeeAccountAdministrationService service(MemoryAccountStore accounts, CapturingAuditStore audit) {
        PasswordHasher passwordHasher = new PasswordHasher() {
            public String hash(String password) { return "hash:" + password; }
            public boolean matches(String password, String hash) { return hash(password).equals(hash); }
        };
        StaffProfileStore profiles = new StaffProfileStore() {
            private final Map<AccountId, StaffProfile> values = new HashMap<>();
            public Optional<StaffProfile> findByAccountId(AccountId id) { return Optional.ofNullable(values.get(id)); }
            public java.util.List<StaffProfile> findAll() { return java.util.List.copyOf(values.values()); }
            public StaffProfile save(StaffProfile profile) { values.put(profile.accountId(), profile); return profile; }
        };
        RefreshSessionStore sessions = new RefreshSessionStore() {
            public RefreshSession save(RefreshSession session) { return session; }
            public Optional<RefreshSession> findByTokenHashForUpdate(String hash) { return Optional.empty(); }
            public int revokeAllForAccount(AccountId id, Instant revokedAt) { return 0; }
        };
        return new EmployeeAccountAdministrationService(accounts, passwordHasher, profiles, sessions, audit, CLOCK);
    }

    private static Account account(Set<AccountRole> roles, AccountStatus status) {
        return new Account(AccountId.newId(), null, new EmailReference(java.util.UUID.randomUUID().toString().replace("-", "").repeat(2), "encrypted-email"),
                "password-hash", status, roles, NOW);
    }

    private static final class MemoryAccountStore implements AccountStore {
        private final Map<AccountId, Account> accounts = new HashMap<>();
        public boolean existsByEmailLookup(String lookup) { return accounts.values().stream().anyMatch(account -> account.email().lookup().equals(lookup)); }
        public boolean existsByCitizenId(CitizenId citizenId) { return false; }
        public Optional<Account> findByEmailLookup(String lookup) { return accounts.values().stream().filter(account -> account.email().lookup().equals(lookup)).findFirst(); }
        public Optional<Account> findById(AccountId accountId) { return Optional.ofNullable(accounts.get(accountId)); }
        public Account updateStatus(AccountId accountId, AccountStatus status) {
            Account account = findById(accountId).orElseThrow();
            Account updated = new Account(account.id(), account.citizenId(), account.email(), account.passwordHash(), status,
                    account.roles(), account.permissions(), account.authorizationVersion() + 1, account.createdAt());
            accounts.put(accountId, updated);
            return updated;
        }
        public Account save(Account account) { accounts.put(account.id(), account); return account; }
    }

    private static final class CapturingAuditStore implements OperationalAuditStore {
        private final ArrayList<String> actions = new ArrayList<>();
        public void record(String actor, String action, String targetType, String targetId, String metadata, Instant occurredAt) { actions.add(action); }
    }
}
