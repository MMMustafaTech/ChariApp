package com.chari.chariapp.additionaldocument.infrastructure.persistence;

import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequestStatus;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

interface SpringDataAdditionalDocumentRequestRepository extends JpaRepository<AdditionalDocumentRequestJpaEntity,String> {
    List<AdditionalDocumentRequestJpaEntity> findByCitizenIdOrderByRequestedAtDesc(String citizenId);
    List<AdditionalDocumentRequestJpaEntity> findByServiceTypeAndRequestIdOrderByRequestedAtDesc(ServiceRequestType type,String requestId);
    boolean existsByServiceTypeAndRequestIdAndStatusIn(ServiceRequestType type,String requestId,Collection<AdditionalDocumentRequestStatus> statuses);
}
