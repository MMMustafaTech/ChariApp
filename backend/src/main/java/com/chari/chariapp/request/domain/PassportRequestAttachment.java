package com.chari.chariapp.request.domain;

import com.chari.chariapp.account.domain.AccountId;

import java.time.Instant;
import java.util.UUID;

/** Immutable metadata for a file attached to a passport service request. */
public record PassportRequestAttachment(
        UUID id,
        UUID requestId,
        AttachmentDocumentType documentType,
        String storageKey,
        String originalFileName,
        String contentType,
        long sizeBytes,
        AccountId uploadedBy,
        Instant uploadedAt
) {
    public PassportRequestAttachment {
        if (documentType == null) {
            throw new IllegalArgumentException("Attachment document type is required");
        }
        if (sizeBytes < 1) {
            throw new IllegalArgumentException("Attachment must not be empty");
        }
    }
}
