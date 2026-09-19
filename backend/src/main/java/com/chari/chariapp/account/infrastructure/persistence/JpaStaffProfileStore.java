package com.chari.chariapp.account.infrastructure.persistence;

import com.chari.chariapp.account.application.port.out.StaffProfileStore;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.StaffProfile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaStaffProfileStore implements StaffProfileStore {
    private final SpringDataStaffProfileRepository repository;

    public JpaStaffProfileStore(SpringDataStaffProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<StaffProfile> findByAccountId(AccountId accountId) {
        return repository.findById(accountId.value().toString()).map(StaffProfileJpaEntity::toDomain);
    }

    @Override
    public List<StaffProfile> findAll() {
        return repository.findAll().stream().map(StaffProfileJpaEntity::toDomain).toList();
    }

    @Override
    public StaffProfile save(StaffProfile profile) {
        StaffProfileJpaEntity entity = repository.findById(profile.accountId().value().toString())
                .orElseGet(() -> StaffProfileJpaEntity.fromDomain(profile));
        entity.apply(profile);
        return repository.save(entity).toDomain();
    }
}
