package com.chari.chariapp.additionaldocument.application.port.out;

import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequest;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.operations.domain.ServiceRequestType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdditionalDocumentRequestStore {
    AdditionalDocumentRequest save(AdditionalDocumentRequest request);
    Optional<AdditionalDocumentRequest> findById(UUID id);
    List<AdditionalDocumentRequest> findByCitizenId(CitizenId citizenId);
    List<AdditionalDocumentRequest> findByOriginalRequest(ServiceRequestType type, UUID requestId);
    boolean hasOpenRequest(ServiceRequestType type, UUID requestId);
}
