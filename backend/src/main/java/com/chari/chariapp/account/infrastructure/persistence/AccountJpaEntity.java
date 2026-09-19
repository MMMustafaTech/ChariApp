package com.chari.chariapp.account.infrastructure.persistence;

import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.EmailReference;
import com.chari.chariapp.account.domain.StaffPermission;
import com.chari.chariapp.citizen.domain.CitizenId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    @Column(length = 36, nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "citizen_id", length = 36, unique = true, columnDefinition = "CHAR(36)")
    private String citizenId;

    @Column(name = "email_lookup", length = 64, nullable = false, unique = true, columnDefinition = "CHAR(64)")
    private String emailLookup;

    @Column(name = "email_ciphertext", nullable = false, columnDefinition = "TEXT")
    private String emailCiphertext;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AccountStatus status;

    @Column(name = "authorization_version", nullable = false)
    private long authorizationVersion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleJpaEntity> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_permissions",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionJpaEntity> permissions = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected AccountJpaEntity() {
    }

    private AccountJpaEntity(Account account, Set<RoleJpaEntity> roles, Set<PermissionJpaEntity> permissions) {
        this.id = account.id().value().toString();
        this.citizenId = account.citizenIdOptional().map(value -> value.value().toString()).orElse(null);
        this.emailLookup = account.email().lookup();
        this.emailCiphertext = account.email().ciphertext();
        this.passwordHash = account.passwordHash();
        this.status = account.status();
        this.authorizationVersion = account.authorizationVersion();
        this.roles = new HashSet<>(roles);
        this.permissions = new HashSet<>(permissions);
        this.createdAt = account.createdAt();
        this.updatedAt = account.createdAt();
    }

    public static AccountJpaEntity fromDomain(Account account, Set<RoleJpaEntity> roles, Set<PermissionJpaEntity> permissions) {
        return new AccountJpaEntity(account, roles, permissions);
    }

    public Account toDomain() {
        Set<AccountRole> accountRoles = roles.stream().map(RoleJpaEntity::getCode).collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<StaffPermission> accountPermissions = permissions.stream().map(PermissionJpaEntity::getCode)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        CitizenId domainCitizenId = citizenId == null ? null : new CitizenId(UUID.fromString(citizenId));
        return new Account(
                new AccountId(UUID.fromString(id)),
                domainCitizenId,
                new EmailReference(emailLookup, emailCiphertext),
                passwordHash,
                status,
                accountRoles,
                accountPermissions,
                authorizationVersion,
                createdAt
        );
    }

    void updateStatus(AccountStatus status) {
        if (this.status != status) {
            this.status = status;
            this.authorizationVersion++;
        }
    }

    void updatePasswordAndInvalidateAuthorization(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }
        this.passwordHash = passwordHash;
        this.authorizationVersion++;
    }

    void invalidateAuthorization() {
        this.authorizationVersion++;
    }

    void updatePermissions(Set<PermissionJpaEntity> permissions) {
        Set<StaffPermission> currentCodes = this.permissions.stream().map(PermissionJpaEntity::getCode)
                .collect(java.util.stream.Collectors.toSet());
        Set<StaffPermission> requestedCodes = permissions.stream().map(PermissionJpaEntity::getCode)
                .collect(java.util.stream.Collectors.toSet());
        if (currentCodes.equals(requestedCodes)) return;
        this.permissions = new HashSet<>(permissions);
        this.authorizationVersion++;
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }
}
