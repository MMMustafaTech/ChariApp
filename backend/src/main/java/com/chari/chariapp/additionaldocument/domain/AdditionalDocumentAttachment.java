package com.chari.chariapp.additionaldocument.domain;

import com.chari.chariapp.account.domain.AccountId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AdditionalDocumentAttachment(
        UUID id, UUID additionalRequestId, String documentName, String storageKey,
        String originalFileName, String contentType, long sizeBytes, AccountId uploadedBy, Instant uploadedAt
) {
    public AdditionalDocumentAttachment {
        Objects.requireNonNull(id); Objects.requireNonNull(additionalRequestId); Objects.requireNonNull(documentName);
        Objects.requireNonNull(storageKey); Objects.requireNonNull(originalFileName); Objects.requireNonNull(contentType);
        Objects.requireNonNull(uploadedBy); Objects.requireNonNull(uploadedAt);
        if (sizeBytes < 1) throw new IllegalArgumentException("Attachment must not be empty");
    }
}
