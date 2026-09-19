package com.chari.chariapp.account.domain;

import com.chari.chariapp.citizen.domain.CitizenId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record Account(
        AccountId id,
        CitizenId citizenId,
        EmailReference email,
        String passwordHash,
        AccountStatus status,
        Set<AccountRole> roles,
        Set<StaffPermission> permissions,
        long authorizationVersion,
        Instant createdAt
) {
    public Account(
            AccountId id,
            CitizenId citizenId,
            EmailReference email,
            String passwordHash,
            AccountStatus status,
            Set<AccountRole> roles,
            Instant createdAt
    ) {
        this(id, citizenId, email, passwordHash, status, roles, Set.of(), 0, createdAt);
    }

    public Account(
            AccountId id,
            CitizenId citizenId,
            EmailReference email,
            String passwordHash,
            AccountStatus status,
            Set<AccountRole> roles,
            long authorizationVersion,
            Instant createdAt
    ) {
        this(id, citizenId, email, passwordHash, status, roles, Set.of(), authorizationVersion, createdAt);
    }

    public Account(
            AccountId id,
            CitizenId citizenId,
            EmailReference email,
            String passwordHash,
            AccountStatus status,
            Set<AccountRole> roles,
            Set<StaffPermission> permissions,
            Instant createdAt
    ) {
        this(id, citizenId, email, passwordHash, status, roles, permissions, 0, createdAt);
    }

    public Account {
        Objects.requireNonNull(id, "Account ID is required");
        Objects.requireNonNull(email, "Email reference is required");
        Objects.requireNonNull(passwordHash, "Password hash is required");
        Objects.requireNonNull(status, "Account status is required");
        Objects.requireNonNull(roles, "Account roles are required");
        Objects.requireNonNull(permissions, "Account permissions are required");
        Objects.requireNonNull(createdAt, "Creation time is required");

        if (passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("An account must have at least one role");
        }
        if (authorizationVersion < 0) {
            throw new IllegalArgumentException("Authorization version must not be negative");
        }
        roles = Set.copyOf(roles);
        permissions = Set.copyOf(permissions);
    }

    public Optional<CitizenId> citizenIdOptional() {
        return Optional.ofNullable(citizenId);
    }
}
