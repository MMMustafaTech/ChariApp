package com.chari.chariapp.additionaldocument.application.port.out;

import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentAttachment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdditionalDocumentAttachmentStore {
    AdditionalDocumentAttachment save(AdditionalDocumentAttachment attachment);
    List<AdditionalDocumentAttachment> findByAdditionalRequestId(UUID requestId);
    Optional<AdditionalDocumentAttachment> findByIdAndAdditionalRequestId(UUID id, UUID requestId);
}
