package com.chari.chariapp.citizen.application.port.out;

import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.PhoneReference;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CitizenStore {

    boolean existsByNationalIdLookup(String nationalIdLookup);

    Optional<Citizen> findByNationalIdLookup(String nationalIdLookup);

    Optional<Citizen> findById(CitizenId citizenId);

    default List<Citizen> findAll() {
        return List.of();
    }

    Citizen updateVerifiedPhone(CitizenId citizenId, PhoneReference verifiedPhone, Instant verifiedAt);

    Citizen save(Citizen citizen);
}
