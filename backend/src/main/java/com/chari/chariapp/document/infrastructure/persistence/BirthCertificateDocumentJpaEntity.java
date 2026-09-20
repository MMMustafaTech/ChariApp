package com.chari.chariapp.document.infrastructure.persistence;

import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.account.domain.AccountId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "birth_certificate_documents")
public class BirthCertificateDocumentJpaEntity {
    @Id @Column(length = 36, columnDefinition = "CHAR(36)") private String id;
    @Column(name = "citizen_id", length = 36, nullable = false, columnDefinition = "CHAR(36)") private String citizenId;
    @Column(name = "certificate_number_lookup", length = 64, nullable = false, unique = true, columnDefinition = "CHAR(64)") private String documentNumberLookup;
    @Column(name = "encrypted_payload", nullable = false, columnDefinition = "TEXT") private String encryptedPayload;
    @Column(nullable = false) private int revision;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by", length = 36, columnDefinition = "CHAR(36)") private String createdBy;
    @Version private Long version;

    protected BirthCertificateDocumentJpaEntity() { }

    public BirthCertificateDocumentJpaEntity(String id, CitizenId citizenId, String lookup, String payload, int revision, Instant createdAt) {
        this(id, citizenId, lookup, payload, revision, createdAt, null);
    }
    public BirthCertificateDocumentJpaEntity(String id, CitizenId citizenId, String lookup, String payload,
                                             int revision, Instant createdAt, AccountId createdBy) {
        this.id = id; this.citizenId = citizenId.value().toString(); this.documentNumberLookup = lookup;
        this.encryptedPayload = payload; this.revision = revision; this.createdAt = createdAt;
        this.createdBy = createdBy == null ? null : createdBy.value().toString();
    }
    public CitizenId citizenId() { return new CitizenId(java.util.UUID.fromString(citizenId)); }
    public String encryptedPayload() { return encryptedPayload; }
    public int revision() { return revision; }
    void update(String lookup, String payload) {
        this.documentNumberLookup = lookup;
        this.encryptedPayload = payload;
    }
}
