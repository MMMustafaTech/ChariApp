package com.chari.chariapp.additionaldocument.infrastructure.persistence;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequest;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequestStatus;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "additional_document_requests")
class AdditionalDocumentRequestJpaEntity {
    @Id @Column(length=36, columnDefinition="CHAR(36)") String id;
    @Enumerated(EnumType.STRING) @Column(name="service_type", nullable=false, length=32) ServiceRequestType serviceType;
    @Column(name="request_id", nullable=false, length=36, columnDefinition="CHAR(36)") String requestId;
    @Column(name="citizen_id", nullable=false, length=36, columnDefinition="CHAR(36)") String citizenId;
    @Column(length=1000) String message;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="additional_document_request_items", joinColumns=@JoinColumn(name="additional_request_id"))
    @Column(name="document_name", nullable=false, length=160)
    Set<String> requiredDocuments = new HashSet<>();
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=32) AdditionalDocumentRequestStatus status;
    @Column(name="requested_by", nullable=false, length=36, columnDefinition="CHAR(36)") String requestedBy;
    @Column(name="requested_at", nullable=false) Instant requestedAt;
    @Column(name="submitted_at") Instant submittedAt;
    @Column(name="resolved_by", length=36, columnDefinition="CHAR(36)") String resolvedBy;
    @Column(name="resolved_at") Instant resolvedAt;
    @Version Long version;

    protected AdditionalDocumentRequestJpaEntity() { }

    static AdditionalDocumentRequestJpaEntity from(AdditionalDocumentRequest request) {
        AdditionalDocumentRequestJpaEntity entity = new AdditionalDocumentRequestJpaEntity();
        entity.id = request.id().toString(); entity.apply(request); return entity;
    }

    void apply(AdditionalDocumentRequest request) {
        serviceType=request.serviceType(); requestId=request.requestId().toString(); citizenId=request.citizenId().value().toString();
        message=request.message(); requiredDocuments=new HashSet<>(request.requiredDocuments()); status=request.status();
        requestedBy=request.requestedBy().value().toString(); requestedAt=request.requestedAt(); submittedAt=request.submittedAt();
        resolvedBy=request.resolvedBy()==null?null:request.resolvedBy().value().toString(); resolvedAt=request.resolvedAt();
    }

    AdditionalDocumentRequest toDomain() {
        return new AdditionalDocumentRequest(UUID.fromString(id), serviceType, UUID.fromString(requestId),
                new CitizenId(UUID.fromString(citizenId)), message, requiredDocuments, status,
                new AccountId(UUID.fromString(requestedBy)), requestedAt, submittedAt,
                resolvedBy==null?null:new AccountId(UUID.fromString(resolvedBy)), resolvedAt);
    }
}
