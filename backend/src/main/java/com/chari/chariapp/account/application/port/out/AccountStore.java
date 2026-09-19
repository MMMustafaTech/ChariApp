package com.chari.chariapp.account.application.port.out;

import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;

import java.util.Optional;
import java.util.List;
import java.util.Set;

public interface AccountStore {

    boolean existsByEmailLookup(String emailLookup);

    boolean existsByCitizenId(CitizenId citizenId);

    Optional<Account> findByEmailLookup(String emailLookup);

    default Optional<Account> findByNationalIdLookup(String nationalIdLookup) {
        return Optional.empty();
    }

    Optional<Account> findById(AccountId accountId);

    default Optional<Account> findByCitizenId(CitizenId citizenId) {
        return Optional.empty();
    }

    default Account updateStatus(AccountId accountId, com.chari.chariapp.account.domain.AccountStatus status) {
        throw new UnsupportedOperationException("Account status updates are not supported");
    }

    /** Changes a password and invalidates every issued access token for the account. */
    default Account updatePasswordAndInvalidateAuthorization(AccountId accountId, String passwordHash) {
        throw new UnsupportedOperationException("Password updates are not supported");
    }

    /** Invalidates every issued access token without changing account details. */
    default Account invalidateAuthorization(AccountId accountId) {
        throw new UnsupportedOperationException("Authorization invalidation is not supported");
    }

    default Account updatePermissions(AccountId accountId, Set<com.chari.chariapp.account.domain.StaffPermission> permissions) {
        throw new UnsupportedOperationException("Permission updates are not supported");
    }

    default List<Account> findStaffAccounts() {
        return List.of();
    }

    Account save(Account account);
}
