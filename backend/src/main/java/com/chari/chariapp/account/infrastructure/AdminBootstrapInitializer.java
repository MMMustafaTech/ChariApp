package com.chari.chariapp.account.infrastructure;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.PasswordHasher;
import com.chari.chariapp.account.application.port.out.StaffProfileStore;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.EmailReference;
import com.chari.chariapp.account.domain.StaffPermission;
import com.chari.chariapp.account.domain.StaffProfile;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@Component
@ConditionalOnProperty(prefix = "app.bootstrap-admin", name = "enabled", havingValue = "true")
public class AdminBootstrapInitializer implements ApplicationRunner {
    private final AccountStore accountStore;
    private final StaffProfileStore profileStore;
    private final PasswordHasher passwordHasher;
    private final PersonalDataProtector dataProtector;
    private final Clock clock;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;

    public AdminBootstrapInitializer(AccountStore accountStore, StaffProfileStore profileStore,
                                     PasswordHasher passwordHasher, PersonalDataProtector dataProtector, Clock clock,
                                     @Value("${app.bootstrap-admin.email:}") String email,
                                     @Value("${app.bootstrap-admin.password:}") String password,
                                     @Value("${app.bootstrap-admin.first-name:Administrator}") String firstName,
                                     @Value("${app.bootstrap-admin.last-name:}") String lastName) {
        this.accountStore = accountStore;
        this.profileStore = profileStore;
        this.passwordHasher = passwordHasher;
        this.dataProtector = dataProtector;
        this.clock = clock;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isBlank() || password == null || password.length() < 12 || password.length() > 128) {
            throw new IllegalStateException("Admin bootstrap requires a valid email and a 12-128 character password");
        }
        String lookup = dataProtector.lookup(normalizedEmail);
        Account admin = accountStore.findByEmailLookup(lookup).orElseGet(() -> accountStore.save(new Account(
                AccountId.newId(), null, new EmailReference(lookup, dataProtector.encrypt(normalizedEmail)),
                passwordHasher.hash(password), AccountStatus.ACTIVE, Set.of(AccountRole.ADMIN),
                StaffPermission.administratorDefaults(), Instant.now(clock))));
        if (!admin.roles().contains(AccountRole.ADMIN)) {
            throw new IllegalStateException("Bootstrap email already belongs to a non-admin account");
        }
        accountStore.updatePermissions(admin.id(), StaffPermission.administratorDefaults());
        if (profileStore.findByAccountId(admin.id()).isEmpty()) {
            Instant now = Instant.now(clock);
            profileStore.save(new StaffProfile(admin.id(), "ADM-" + admin.id().value().toString().replace("-", "")
                    .substring(0, 8).toUpperCase(Locale.ROOT), firstName, lastName, null, "Administrator", now, now));
        }
    }
}
