package com.chari.chariapp.identityrequest.domain;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.request.domain.AttachmentDocumentType;
import java.time.Instant;
import java.util.UUID;

/** Immutable metadata for a protected national-identity request attachment. */
public record NationalIdentityRequestAttachment(UUID id, UUID requestId, AttachmentDocumentType documentType,
                                                String storageKey, String originalFileName,
                                                String contentType, long sizeBytes, AccountId uploadedBy, Instant uploadedAt) {
    public NationalIdentityRequestAttachment {
        if (documentType == null) throw new IllegalArgumentException("Attachment document type is required");
        if (sizeBytes < 1) throw new IllegalArgumentException("Attachment must not be empty");
    }
}
