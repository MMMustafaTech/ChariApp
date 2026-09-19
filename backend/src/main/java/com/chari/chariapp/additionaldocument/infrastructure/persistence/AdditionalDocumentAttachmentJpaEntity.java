package com.chari.chariapp.additionaldocument.infrastructure.persistence;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentAttachment;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="additional_document_attachments")
class AdditionalDocumentAttachmentJpaEntity {
    @Id @Column(length=36,columnDefinition="CHAR(36)") String id;
    @Column(name="additional_request_id",nullable=false,length=36,columnDefinition="CHAR(36)") String additionalRequestId;
    @Column(name="document_name",nullable=false,length=160) String documentName;
    @Column(name="storage_key",nullable=false,unique=true,length=255) String storageKey;
    @Column(name="original_file_name",nullable=false,length=255) String originalFileName;
    @Column(name="content_type",nullable=false,length=100) String contentType;
    @Column(name="size_bytes",nullable=false) long sizeBytes;
    @Column(name="uploaded_by",nullable=false,length=36,columnDefinition="CHAR(36)") String uploadedBy;
    @Column(name="uploaded_at",nullable=false) Instant uploadedAt;
    protected AdditionalDocumentAttachmentJpaEntity(){}
    static AdditionalDocumentAttachmentJpaEntity from(AdditionalDocumentAttachment a){var e=new AdditionalDocumentAttachmentJpaEntity();e.id=a.id().toString();e.additionalRequestId=a.additionalRequestId().toString();e.documentName=a.documentName();e.storageKey=a.storageKey();e.originalFileName=a.originalFileName();e.contentType=a.contentType();e.sizeBytes=a.sizeBytes();e.uploadedBy=a.uploadedBy().value().toString();e.uploadedAt=a.uploadedAt();return e;}
    AdditionalDocumentAttachment toDomain(){return new AdditionalDocumentAttachment(UUID.fromString(id),UUID.fromString(additionalRequestId),documentName,storageKey,originalFileName,contentType,sizeBytes,new AccountId(UUID.fromString(uploadedBy)),uploadedAt);}
}
