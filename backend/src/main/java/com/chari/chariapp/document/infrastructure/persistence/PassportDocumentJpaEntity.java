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
@Table(name = "passport_documents")
public class PassportDocumentJpaEntity {
    @Id @Column(length = 36, columnDefinition = "CHAR(36)") private String id;
    @Column(name = "citizen_id", length = 36, nullable = false, columnDefinition = "CHAR(36)") private String citizenId;
    @Column(name = "passport_number_lookup", length = 64, nullable = false, unique = true, columnDefinition = "CHAR(64)") private String documentNumberLookup;
    @Column(name = "encrypted_payload", nullable = false, columnDefinition = "TEXT") private String encryptedPayload;
    @Column(nullable = false, length = 32) private String status;
    @Column(name = "issued_on") private java.time.LocalDate issuedOn;
    @Column(name = "expires_on") private java.time.LocalDate expiresOn;
    @Column(nullable = false) private int revision;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by", length = 36, columnDefinition = "CHAR(36)") private String createdBy;
    @Version private Long version;

    protected PassportDocumentJpaEntity() { }

    public PassportDocumentJpaEntity(String id, CitizenId citizenId, String lookup, String payload, String status,
                                     java.time.LocalDate issuedOn, java.time.LocalDate expiresOn, int revision, Instant createdAt) {
        this(id, citizenId, lookup, payload, status, issuedOn, expiresOn, revision, createdAt, null);
    }
    public PassportDocumentJpaEntity(String id, CitizenId citizenId, String lookup, String payload, String status,
                                     java.time.LocalDate issuedOn, java.time.LocalDate expiresOn, int revision,
                                     Instant createdAt, AccountId createdBy) {
        this.id = id; this.citizenId = citizenId.value().toString(); this.documentNumberLookup = lookup;
        this.encryptedPayload = payload; this.status = status; this.issuedOn = issuedOn; this.expiresOn = expiresOn;
        this.revision = revision; this.createdAt = createdAt;
        this.createdBy = createdBy == null ? null : createdBy.value().toString();
    }
    public CitizenId citizenId() { return new CitizenId(java.util.UUID.fromString(citizenId)); }
    public String encryptedPayload() { return encryptedPayload; }
    public int revision() { return revision; }
}
