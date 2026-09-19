package com.chari.chariapp.document.application.port.out;

import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.citizen.domain.CitizenId;

import java.util.Optional;
import java.util.List;
import com.chari.chariapp.document.domain.DependentBirthCertificate;

public interface CitizenDocumentReadStore {

    Optional<MyPassport> findPassportByCitizenId(CitizenId citizenId);

    Optional<MyNationalIdentity> findNationalIdentityByCitizenId(CitizenId citizenId);

    Optional<MyBirthCertificate> findBirthCertificateByCitizenId(CitizenId citizenId);

    default List<DependentBirthCertificate> findDependentBirthCertificatesByCitizenId(CitizenId citizenId) {
        return List.of();
    }

    default Optional<DependentBirthCertificate> findDependentBirthCertificateById(java.util.UUID id) {
        return Optional.empty();
    }
}
