package com.chari.chariapp.request.infrastructure.persistence;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.request.domain.PassportRequestAttachment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "request_attachments")
class PassportRequestAttachmentJpaEntity {
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private String id;
    @Column(name = "service_request_id", length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String requestId;
    @Column(name = "document_type", length = 64, nullable = false)
    private String documentType;
    @Column(name = "storage_key", length = 128, nullable = false, unique = true)
    private String storageKey;
    @Column(name = "original_file_name", length = 255, nullable = false)
    private String originalFileName;
    @Column(name = "content_type", length = 128, nullable = false)
    private String contentType;
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;
    @Column(name = "uploaded_by", length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String uploadedBy;
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
    @Version
    private Long version;

    protected PassportRequestAttachmentJpaEntity() { }

    private PassportRequestAttachmentJpaEntity(PassportRequestAttachment attachment) {
        id = attachment.id().toString();
        requestId = attachment.requestId().toString();
        documentType = attachment.documentType().name();
        storageKey = attachment.storageKey();
        originalFileName = attachment.originalFileName();
        contentType = attachment.contentType();
        sizeBytes = attachment.sizeBytes();
        uploadedBy = attachment.uploadedBy().value().toString();
        uploadedAt = attachment.uploadedAt();
    }

    static PassportRequestAttachmentJpaEntity from(PassportRequestAttachment attachment) {
        return new PassportRequestAttachmentJpaEntity(attachment);
    }

    PassportRequestAttachment toDomain() {
        return new PassportRequestAttachment(UUID.fromString(id), UUID.fromString(requestId),
                com.chari.chariapp.request.domain.AttachmentDocumentType.valueOf(documentType), storageKey, originalFileName,
                contentType, sizeBytes, new AccountId(UUID.fromString(uploadedBy)), uploadedAt);
    }
}
