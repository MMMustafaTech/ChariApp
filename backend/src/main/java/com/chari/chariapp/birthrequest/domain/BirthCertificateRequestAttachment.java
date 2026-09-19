package com.chari.chariapp.birthrequest.domain;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.request.domain.AttachmentDocumentType;
import java.time.Instant;
import java.util.UUID;

/** Immutable metadata for a protected birth-certificate request attachment. */
public record BirthCertificateRequestAttachment(UUID id, UUID requestId, AttachmentDocumentType documentType,
                                                String storageKey, String originalFileName,
                                                String contentType, long sizeBytes, AccountId uploadedBy, Instant uploadedAt) {
    public BirthCertificateRequestAttachment {
        if (documentType == null) throw new IllegalArgumentException("Attachment document type is required");
        if (sizeBytes < 1) throw new IllegalArgumentException("Attachment must not be empty");
    }
}
