package com.chari.chariapp.identityrequest.infrastructure.persistence;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequest;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestKind;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestStatus;
import com.chari.chariapp.request.infrastructure.persistence.EncryptedServiceRequestPayloadCodec;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_requests")
public class NationalIdentityRequestJpaEntity {
    static final String TYPE = "NATIONAL_IDENTITY";
    @Id @Column(length = 36, columnDefinition = "CHAR(36)") private String id;
    @Column(name = "citizen_id", length = 36, nullable = false, columnDefinition = "CHAR(36)") private String citizenId;
    @Column(nullable = false, length = 64) private String type;
    @Enumerated(EnumType.STRING) @Column(name = "request_kind", nullable = false, length = 32) private NationalIdentityRequestKind requestKind;
    @Column(name = "request_reason", length = 1000) private String requestReason;
    @Column(name = "request_payload", columnDefinition = "TEXT") private String requestPayload;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private NationalIdentityRequestStatus status;
    @Column(name = "submitted_at", nullable = false) private Instant submittedAt;
    @Column(name = "reviewed_by", length = 36, columnDefinition = "CHAR(36)") private String reviewedBy;
    @Column(name = "reviewed_at") private Instant reviewedAt;
    @Column(name = "decision_reason", length = 1000) private String decisionReason;
    @Column(name = "open_request_key", length = 36, columnDefinition = "CHAR(36)") private String openRequestKey;
    @Column(name = "open_request_type", length = 64) private String openRequestType;
    @Version private Long version;

    protected NationalIdentityRequestJpaEntity() { }
    private NationalIdentityRequestJpaEntity(NationalIdentityRequest request, EncryptedServiceRequestPayloadCodec payloadCodec) { apply(request, payloadCodec); }
    static NationalIdentityRequestJpaEntity from(NationalIdentityRequest request, EncryptedServiceRequestPayloadCodec payloadCodec) { return new NationalIdentityRequestJpaEntity(request, payloadCodec); }

    void apply(NationalIdentityRequest request, EncryptedServiceRequestPayloadCodec payloadCodec) {
        id = request.id().toString(); citizenId = request.citizenId().value().toString(); type = TYPE;
        requestKind = request.kind(); requestReason = request.requestReason(); requestPayload = payloadCodec.encrypt(request.submissionDetails()); status = request.status(); submittedAt = request.submittedAt();
        reviewedBy = request.reviewedBy() == null ? null : request.reviewedBy().value().toString(); reviewedAt = request.reviewedAt();
        decisionReason = request.decisionReason(); openRequestKey = request.status().isOpen() ? citizenId : null;
        openRequestType = request.status().isOpen() ? TYPE : null;
    }

    NationalIdentityRequest toDomain(EncryptedServiceRequestPayloadCodec payloadCodec) {
        return new NationalIdentityRequest(UUID.fromString(id), new CitizenId(UUID.fromString(citizenId)), requestKind,
                requestReason, payloadCodec.decrypt(requestPayload), status, reviewedBy == null ? null : new AccountId(UUID.fromString(reviewedBy)),
                submittedAt, reviewedAt, decisionReason);
    }

    boolean isNationalIdentityRequest() {
        return TYPE.equals(type);
    }
}
