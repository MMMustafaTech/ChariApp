package com.chari.chariapp.document.infrastructure.persistence;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dependent_birth_certificate_documents")
public class DependentBirthCertificateDocumentJpaEntity {
    @Id @Column(length = 36, columnDefinition = "CHAR(36)") private String id;
    @Column(name = "request_id", length = 36, nullable = false, unique = true, columnDefinition = "CHAR(36)") private String requestId;
    @Column(name = "parent_citizen_id", length = 36, nullable = false, columnDefinition = "CHAR(36)") private String parentCitizenId;
    @Column(name = "certificate_number_lookup", length = 64, nullable = false, unique = true, columnDefinition = "CHAR(64)") private String documentNumberLookup;
    @Column(name = "encrypted_payload", nullable = false, columnDefinition = "TEXT") private String encryptedPayload;
    @Column(name = "created_by", length = 36, nullable = false, columnDefinition = "CHAR(36)") private String createdBy;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Version private Long version;

    protected DependentBirthCertificateDocumentJpaEntity() {
    }

    public DependentBirthCertificateDocumentJpaEntity(UUID id, UUID requestId, CitizenId parentCitizenId,
                                                       String lookup, String payload, AccountId createdBy,
                                                       Instant createdAt) {
        this.id = id.toString();
        this.requestId = requestId.toString();
        this.parentCitizenId = parentCitizenId.value().toString();
        this.documentNumberLookup = lookup;
        this.encryptedPayload = payload;
        this.createdBy = createdBy.value().toString();
        this.createdAt = createdAt;
    }

    public UUID id() { return UUID.fromString(id); }
    public UUID requestId() { return UUID.fromString(requestId); }
    public CitizenId parentCitizenId() { return new CitizenId(UUID.fromString(parentCitizenId)); }
    public String encryptedPayload() { return encryptedPayload; }
    public Instant createdAt() { return createdAt; }
}
