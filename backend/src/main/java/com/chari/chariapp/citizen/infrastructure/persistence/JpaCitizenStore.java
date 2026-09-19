package com.chari.chariapp.citizen.infrastructure.persistence;

import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.PhoneReference;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaCitizenStore implements CitizenStore {

    private final SpringDataCitizenRepository repository;

    public JpaCitizenStore(SpringDataCitizenRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByNationalIdLookup(String nationalIdLookup) {
        return repository.existsByNationalIdLookup(nationalIdLookup);
    }

    @Override
    public Optional<Citizen> findByNationalIdLookup(String nationalIdLookup) {
        return repository.findByNationalIdLookup(nationalIdLookup).map(CitizenJpaEntity::toDomain);
    }

    @Override
    public Optional<Citizen> findById(com.chari.chariapp.citizen.domain.CitizenId citizenId) {
        return repository.findById(citizenId.value().toString()).map(CitizenJpaEntity::toDomain);
    }

    @Override
    public List<Citizen> findAll() {
        return repository.findAll().stream().map(CitizenJpaEntity::toDomain).toList();
    }

    @Override
    public Citizen updateVerifiedPhone(CitizenId citizenId, PhoneReference verifiedPhone, Instant verifiedAt) {
        CitizenJpaEntity entity = repository.findById(citizenId.value().toString())
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));
        entity.replaceVerifiedPhone(verifiedPhone, verifiedAt);
        return repository.save(entity).toDomain();
    }

    @Override
    public Citizen save(Citizen citizen) {
        return repository.save(CitizenJpaEntity.fromDomain(citizen)).toDomain();
    }
}
