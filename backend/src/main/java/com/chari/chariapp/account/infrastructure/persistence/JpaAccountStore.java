package com.chari.chariapp.account.infrastructure.persistence;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.citizen.domain.CitizenId;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.Optional;

@Repository
public class JpaAccountStore implements AccountStore {

    private final SpringDataAccountRepository accountRepository;
    private final SpringDataRoleRepository roleRepository;

    public JpaAccountStore(SpringDataAccountRepository accountRepository, SpringDataRoleRepository roleRepository) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public boolean existsByEmailLookup(String emailLookup) {
        return accountRepository.existsByEmailLookup(emailLookup);
    }

    @Override
    public boolean existsByCitizenId(CitizenId citizenId) {
        return accountRepository.existsByCitizenId(citizenId.value().toString());
    }

    @Override
    public Optional<Account> findByEmailLookup(String emailLookup) {
        return accountRepository.findByEmailLookup(emailLookup).map(AccountJpaEntity::toDomain);
    }

    @Override
    public Optional<Account> findByNationalIdLookup(String nationalIdLookup) {
        return accountRepository.findByNationalIdLookup(nationalIdLookup).map(AccountJpaEntity::toDomain);
    }

    @Override
    public Optional<Account> findById(com.chari.chariapp.account.domain.AccountId accountId) {
        return accountRepository.findById(accountId.value().toString()).map(AccountJpaEntity::toDomain);
    }

    @Override
    public Account updateStatus(AccountId accountId, AccountStatus status) {
        AccountJpaEntity entity = accountRepository.findById(accountId.value().toString())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        entity.updateStatus(status);
        return accountRepository.save(entity).toDomain();
    }

    @Override
    public Account updatePasswordAndInvalidateAuthorization(AccountId accountId, String passwordHash) {
        AccountJpaEntity entity = accountRepository.findById(accountId.value().toString())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        entity.updatePasswordAndInvalidateAuthorization(passwordHash);
        return accountRepository.save(entity).toDomain();
    }

    @Override
    public Account invalidateAuthorization(AccountId accountId) {
        AccountJpaEntity entity = accountRepository.findById(accountId.value().toString())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        entity.invalidateAuthorization();
        return accountRepository.save(entity).toDomain();
    }

    @Override
    public Account save(Account account) {
        Set<AccountRole> requestedRoles = account.roles();
        Set<RoleJpaEntity> roles = Set.copyOf(roleRepository.findByCodeIn(requestedRoles));
        if (roles.size() != requestedRoles.size()) {
            throw new IllegalStateException("One or more requested account roles are not configured");
        }
        return accountRepository.save(AccountJpaEntity.fromDomain(account, roles)).toDomain();
    }
}
