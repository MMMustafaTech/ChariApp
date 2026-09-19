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
import com.chari.chariapp.account.domain.StaffPermission;
import com.chari.chariapp.account.domain.StaffProfile;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.List;

/** Administrative boundary: an admin may provision and disable EMPLOYEE accounts, never grant ADMIN. */
public class EmployeeAccountAdministrationService implements EmployeeAccountAdministrationUseCase {
    private final AccountStore accountStore;
    private final PasswordHasher passwordHasher;
    private final OperationalAuditStore auditStore;
    private final StaffProfileStore staffProfileStore;
    private final RefreshSessionStore refreshSessionStore;
    private final Clock clock;

    public EmployeeAccountAdministrationService(
            AccountStore accountStore, PasswordHasher passwordHasher, StaffProfileStore staffProfileStore,
            RefreshSessionStore refreshSessionStore, OperationalAuditStore auditStore, Clock clock
    ) {
        this.accountStore = Objects.requireNonNull(accountStore, "Account store is required");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "Password hasher is required");
        this.staffProfileStore = Objects.requireNonNull(staffProfileStore, "Staff profile store is required");
        this.refreshSessionStore = Objects.requireNonNull(refreshSessionStore, "Refresh session store is required");
        this.auditStore = Objects.requireNonNull(auditStore, "Audit store is required");
        this.clock = Objects.requireNonNull(clock, "Clock is required");
    }

    @Override
    @Transactional
    public AccountId provisionEmployee(ProvisionEmployeeAccountCommand command) {
        Objects.requireNonNull(command, "Employee provisioning command is required");
        requireActiveAdministrator(command.administratorId());
        if (command.rawPassword() == null || command.rawPassword().length() < 12 || command.rawPassword().length() > 128) {
            throw new WeakPasswordException();
        }
        EmailReference email = new EmailReference(command.emailLookup(), command.encryptedEmail());
        if (accountStore.existsByEmailLookup(email.lookup())) {
            throw new AccountAlreadyExistsException("email");
        }
        Instant now = Instant.now(clock);
        Set<StaffPermission> permissions = command.permissions() == null
                ? StaffPermission.employeeDefaults() : Set.copyOf(command.permissions());
        Account employee = new Account(AccountId.newId(), null, email, passwordHasher.hash(command.rawPassword()),
                AccountStatus.ACTIVE, Set.of(AccountRole.EMPLOYEE), permissions, now);
        Account saved = accountStore.save(employee);
        staffProfileStore.save(new StaffProfile(saved.id(), employeeNumber(saved.id()), command.firstName(),
                command.lastName(), command.phone(), command.jobTitle(), now, now));
        auditStore.record(command.administratorId().value().toString(), "EMPLOYEE_ACCOUNT_PROVISIONED", "ACCOUNT",
                saved.id().value().toString(), null, now);
        return saved.id();
    }

    @Override
    @Transactional
    public void changeEmployeeStatus(ChangeEmployeeAccountStatusCommand command) {
        Objects.requireNonNull(command, "Employee status command is required");
        requireActiveAdministrator(command.administratorId());
        if (command.administratorId().equals(command.employeeId())
                || (command.status() != AccountStatus.ACTIVE && command.status() != AccountStatus.DISABLED)) {
            throw new IllegalArgumentException("Invalid employee status change");
        }
        Account employee = accountStore.findById(command.employeeId()).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!employee.roles().equals(Set.of(AccountRole.EMPLOYEE))) {
            throw new IllegalArgumentException("Only employee accounts can be managed here");
        }
        accountStore.updateStatus(command.employeeId(), command.status());
        auditStore.record(command.administratorId().value().toString(), "EMPLOYEE_ACCOUNT_STATUS_CHANGED", "ACCOUNT",
                command.employeeId().value().toString(), "{\"status\":\"" + command.status() + "\"}", Instant.now(clock));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffAccountView> listStaff(AccountId administratorId) {
        requireActiveAdministrator(administratorId);
        return accountStore.findStaffAccounts().stream().map(account -> new StaffAccountView(
                account, staffProfileStore.findByAccountId(account.id()).orElse(null))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffAccountView getStaff(AccountId administratorId, AccountId staffAccountId) {
        requireActiveAdministrator(administratorId);
        Account account = requireStaff(staffAccountId);
        return new StaffAccountView(account, staffProfileStore.findByAccountId(staffAccountId).orElse(null));
    }

    @Override
    @Transactional
    public void updateEmployee(UpdateStaffAccountCommand command) {
        Objects.requireNonNull(command, "Staff update command is required");
        requireActiveAdministrator(command.administratorId());
        Account employee = requireEmployee(command.employeeId());
        Instant now = Instant.now(clock);
        StaffProfile profile = staffProfileStore.findByAccountId(employee.id()).orElseGet(() ->
                new StaffProfile(employee.id(), employeeNumber(employee.id()), null, null, null, null, now, now));
        staffProfileStore.save(profile.update(command.firstName(), command.lastName(), command.phone(), command.jobTitle(), now));
        accountStore.updatePermissions(employee.id(), command.permissions() == null ? employee.permissions() : command.permissions());
        refreshSessionStore.revokeAllForAccount(employee.id(), now);
        auditStore.record(command.administratorId().value().toString(), "EMPLOYEE_ACCOUNT_UPDATED", "ACCOUNT",
                employee.id().value().toString(), null, now);
    }

    @Override
    @Transactional
    public void resetEmployeePassword(ResetEmployeePasswordCommand command) {
        Objects.requireNonNull(command, "Password reset command is required");
        requireActiveAdministrator(command.administratorId());
        requireEmployee(command.employeeId());
        if (command.temporaryPassword() == null || command.temporaryPassword().length() < 12
                || command.temporaryPassword().length() > 128) throw new WeakPasswordException();
        Instant now = Instant.now(clock);
        accountStore.updatePasswordAndInvalidateAuthorization(command.employeeId(), passwordHasher.hash(command.temporaryPassword()));
        refreshSessionStore.revokeAllForAccount(command.employeeId(), now);
        auditStore.record(command.administratorId().value().toString(), "EMPLOYEE_PASSWORD_RESET", "ACCOUNT",
                command.employeeId().value().toString(), null, now);
    }

    private void requireActiveAdministrator(AccountId accountId) {
        Account administrator = accountStore.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Administrator unavailable"));
        if (administrator.status() != AccountStatus.ACTIVE || !administrator.roles().contains(AccountRole.ADMIN)) {
            throw new IllegalArgumentException("Administrator unavailable");
        }
    }

    private Account requireStaff(AccountId accountId) {
        Account account = accountStore.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Staff account not found"));
        if (account.roles().stream().noneMatch(role -> role == AccountRole.ADMIN || role == AccountRole.EMPLOYEE)) {
            throw new IllegalArgumentException("Staff account not found");
        }
        return account;
    }

    private Account requireEmployee(AccountId accountId) {
        Account account = requireStaff(accountId);
        if (!account.roles().equals(Set.of(AccountRole.EMPLOYEE))) {
            throw new IllegalArgumentException("Only employee accounts can be changed here");
        }
        return account;
    }

    private static String employeeNumber(AccountId id) {
        return "EMP-" + id.value().toString().replace("-", "").substring(0, 8).toUpperCase(java.util.Locale.ROOT);
    }
}
