package com.chari.chariapp.birthrequest.infrastructure.persistence;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.birthrequest.domain.*;
import com.chari.chariapp.citizen.domain.CitizenId;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "service_requests")
public class BirthCertificateRequestJpaEntity {
    static final String TYPE = "BIRTH_CERTIFICATE";
    @Id @Column(length = 36, columnDefinition = "CHAR(36)") private String id;
    @Column(name = "citizen_id", length = 36, nullable = false, columnDefinition = "CHAR(36)") private String citizenId;
    @Column(nullable = false, length = 64) private String type;
    @Enumerated(EnumType.STRING) @Column(name = "request_kind", nullable = false, length = 32) private BirthCertificateRequestKind requestKind;
    @Column(name = "request_reason", length = 1000) private String requestReason;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private BirthCertificateRequestStatus status;
    @Column(name = "submitted_at", nullable = false) private Instant submittedAt;
    @Column(name = "reviewed_by", length = 36, columnDefinition = "CHAR(36)") private String reviewedBy;
    @Column(name = "reviewed_at") private Instant reviewedAt;
    @Column(name = "decision_reason", length = 1000) private String decisionReason;
    @Column(name = "open_request_key", length = 36, columnDefinition = "CHAR(36)") private String openRequestKey;
    @Column(name = "open_request_type", length = 64) private String openRequestType;
    @Version private Long version;
    protected BirthCertificateRequestJpaEntity() { }
    private BirthCertificateRequestJpaEntity(BirthCertificateRequest request) { apply(request); }
    static BirthCertificateRequestJpaEntity from(BirthCertificateRequest request) { return new BirthCertificateRequestJpaEntity(request); }
    void apply(BirthCertificateRequest request) { id=request.id().toString(); citizenId=request.citizenId().value().toString(); type=TYPE; requestKind=request.kind(); requestReason=request.requestReason(); status=request.status(); submittedAt=request.submittedAt(); reviewedBy=request.reviewedBy()==null?null:request.reviewedBy().value().toString(); reviewedAt=request.reviewedAt(); decisionReason=request.decisionReason(); openRequestKey=request.status().isOpen()?citizenId:null; openRequestType=request.status().isOpen()?openRequestType(request.kind()):null; }
    static String openRequestType(BirthCertificateRequestKind kind) { return TYPE + ":" + kind.name(); }
    BirthCertificateRequest toDomain() { return new BirthCertificateRequest(UUID.fromString(id),new CitizenId(UUID.fromString(citizenId)),requestKind,requestReason,status,reviewedBy==null?null:new AccountId(UUID.fromString(reviewedBy)),submittedAt,reviewedAt,decisionReason); }
    boolean isBirthCertificateRequest() { return TYPE.equals(type); }
}
