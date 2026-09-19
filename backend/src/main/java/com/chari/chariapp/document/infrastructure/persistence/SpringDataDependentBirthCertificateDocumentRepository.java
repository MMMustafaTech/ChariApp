package com.chari.chariapp.document.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataDependentBirthCertificateDocumentRepository
        extends JpaRepository<DependentBirthCertificateDocumentJpaEntity, String> {
    List<DependentBirthCertificateDocumentJpaEntity> findByParentCitizenIdOrderByCreatedAtDesc(String parentCitizenId);
}
