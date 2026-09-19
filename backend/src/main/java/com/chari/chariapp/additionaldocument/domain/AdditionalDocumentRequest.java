package com.chari.chariapp.additionaldocument.domain;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.operations.domain.ServiceRequestType;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record AdditionalDocumentRequest(
        UUID id, ServiceRequestType serviceType, UUID requestId, CitizenId citizenId,
        String message, Set<String> requiredDocuments, AdditionalDocumentRequestStatus status,
        AccountId requestedBy, Instant requestedAt, Instant submittedAt,
        AccountId resolvedBy, Instant resolvedAt
) {
    public AdditionalDocumentRequest {
        Objects.requireNonNull(id); Objects.requireNonNull(serviceType); Objects.requireNonNull(requestId);
        Objects.requireNonNull(citizenId); Objects.requireNonNull(requiredDocuments); Objects.requireNonNull(status);
        Objects.requireNonNull(requestedBy); Objects.requireNonNull(requestedAt);
        message = normalize(message, 1000, "Message");
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        requiredDocuments.forEach(value -> normalized.add(normalizeRequired(value)));
        if (normalized.isEmpty() || normalized.size() > 20) throw new IllegalArgumentException("Required documents must contain 1-20 items");
        requiredDocuments = Set.copyOf(normalized);
        if (status == AdditionalDocumentRequestStatus.REQUESTED && (submittedAt != null || resolvedBy != null || resolvedAt != null)) throw new IllegalArgumentException("Requested documents cannot contain completion data");
        if (status == AdditionalDocumentRequestStatus.SUBMITTED && (submittedAt == null || resolvedBy != null || resolvedAt != null)) throw new IllegalArgumentException("Submitted documents require a submission time");
        if (status == AdditionalDocumentRequestStatus.RESOLVED && (submittedAt == null || resolvedBy == null || resolvedAt == null)) throw new IllegalArgumentException("Resolved documents require resolution data");
    }

    public static AdditionalDocumentRequest create(ServiceRequestType type, UUID requestId, CitizenId citizenId,
                                                     String message, Set<String> documents, AccountId actor, Instant now) {
        return new AdditionalDocumentRequest(UUID.randomUUID(), type, requestId, citizenId, message, documents,
                AdditionalDocumentRequestStatus.REQUESTED, actor, now, null, null, null);
    }

    public AdditionalDocumentRequest submit(Instant now) {
        if (status != AdditionalDocumentRequestStatus.REQUESTED) throw new AdditionalDocumentRequestConflictException("ADDITIONAL_DOCUMENTS_NOT_AWAITING_UPLOAD");
        return new AdditionalDocumentRequest(id, serviceType, requestId, citizenId, message, requiredDocuments,
                AdditionalDocumentRequestStatus.SUBMITTED, requestedBy, requestedAt, now, null, null);
    }

    public AdditionalDocumentRequest resolve(AccountId actor, Instant now) {
        if (status != AdditionalDocumentRequestStatus.SUBMITTED) throw new AdditionalDocumentRequestConflictException("ADDITIONAL_DOCUMENTS_NOT_SUBMITTED");
        return new AdditionalDocumentRequest(id, serviceType, requestId, citizenId, message, requiredDocuments,
                AdditionalDocumentRequestStatus.RESOLVED, requestedBy, requestedAt, submittedAt, actor, now);
    }

    private static String normalizeRequired(String value) {
        String normalized = normalize(value, 160, "Document name");
        if (normalized == null) throw new IllegalArgumentException("Document name is required");
        return normalized;
    }

    private static String normalize(String value, int max, String label) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > max) throw new IllegalArgumentException(label + " is too long");
        return normalized;
    }
}
