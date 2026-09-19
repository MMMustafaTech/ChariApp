package com.chari.chariapp.request.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.request.application.port.out.AttachmentContentStore;
import com.chari.chariapp.request.application.port.out.PassportRequestAttachmentStore;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.request.domain.PassportRequest;
import com.chari.chariapp.request.domain.PassportRequestAttachment;
import com.chari.chariapp.request.domain.PassportRequestStatus;
import com.chari.chariapp.request.domain.AttachmentDocumentType;
import com.chari.chariapp.request.domain.RequestBeneficiaryType;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Owns authorization, validation and metadata for passport-request attachments. */
public class PassportRequestAttachmentService {

    private final PassportRequestActorAccess actorAccess;
    private final PassportRequestStore requestStore;
    private final PassportRequestAttachmentStore attachmentStore;
    private final AttachmentContentStore contentStore;
    private final OperationalAuditStore auditStore;
    private final Clock clock;

    public PassportRequestAttachmentService(
            PassportRequestActorAccess actorAccess,
            PassportRequestStore requestStore,
            PassportRequestAttachmentStore attachmentStore,
            AttachmentContentStore contentStore,
            OperationalAuditStore auditStore,
            Clock clock
    ) {
        this.actorAccess = actorAccess;
        this.requestStore = requestStore;
        this.attachmentStore = attachmentStore;
        this.contentStore = contentStore;
        this.auditStore = auditStore;
        this.clock = clock;
    }

    public PassportRequestAttachment upload(AccountId actorId, UUID requestId,
                                              AttachmentDocumentType documentType, AttachmentUpload upload) {
        CitizenId citizenId = actorAccess.requireActiveCitizen(actorId);
        PassportRequest request = citizenOwnedRequest(citizenId, requestId);
        if (request.status() != PassportRequestStatus.SUBMITTED) {
            throw new PassportRequestConflictException(PassportRequestConflictException.Reason.ATTACHMENTS_CLOSED);
        }

        String contentType = AttachmentUploadPolicy.validateMetadata(upload);
        String storageKey = UUID.randomUUID().toString();
        Instant uploadedAt = Instant.now(clock);
        PassportRequestAttachment attachment = new PassportRequestAttachment(
                UUID.randomUUID(), requestId, documentType, storageKey,
                AttachmentUploadPolicy.safeFileName(upload.originalFileName()),
                contentType, upload.sizeBytes(), actorId, uploadedAt
        );

        try (BufferedInputStream content = new BufferedInputStream(upload.content())) {
            AttachmentUploadPolicy.validateSignature(content, contentType);
            contentStore.store(storageKey, content);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read attachment", exception);
        }

        try {
            PassportRequestAttachment saved = attachmentStore.save(attachment);
            auditStore.record(actorId.value().toString(), "PASSPORT_REQUEST_ATTACHMENT_UPLOADED",
                    "SERVICE_REQUEST", requestId.toString(), null, uploadedAt);
            return saved;
        } catch (RuntimeException exception) {
            contentStore.delete(storageKey);
            throw exception;
        }
    }

    public List<PassportRequestAttachment> listMine(AccountId actorId, UUID requestId) {
        CitizenId citizenId = actorAccess.requireActiveCitizen(actorId);
        citizenOwnedRequest(citizenId, requestId);
        return attachmentStore.findByRequestId(requestId);
    }

    public AttachmentContent downloadMine(AccountId actorId, UUID requestId, UUID attachmentId) {
        CitizenId citizenId = actorAccess.requireActiveCitizen(actorId);
        citizenOwnedRequest(citizenId, requestId);
        return content(requestId, attachmentId);
    }

    public List<PassportRequestAttachment> listForOperations(AccountId actorId, UUID requestId) {
        actorAccess.requireActiveOperator(actorId);
        requireRequest(requestId);
        return attachmentStore.findByRequestId(requestId);
    }

    public AttachmentContent downloadForOperations(AccountId actorId, UUID requestId, UUID attachmentId) {
        actorAccess.requireActiveOperator(actorId);
        requireRequest(requestId);
        return content(requestId, attachmentId);
    }

    public AttachmentRequirements requirementsMine(AccountId actorId, UUID requestId) {
        CitizenId citizenId = actorAccess.requireActiveCitizen(actorId);
        return requirements(citizenOwnedRequest(citizenId, requestId));
    }

    public AttachmentRequirements requirementsForOperations(AccountId actorId, UUID requestId) {
        actorAccess.requireActiveOperator(actorId);
        return requirements(requireRequest(requestId));
    }

    public void requireComplete(PassportRequest request) {
        if (!requirements(request).complete()) {
            throw new PassportRequestConflictException(
                    PassportRequestConflictException.Reason.MISSING_REQUIRED_ATTACHMENTS);
        }
    }

    private AttachmentRequirements requirements(PassportRequest request) {
        RequestBeneficiaryType beneficiaryType = request.submissionDetails() == null
                ? RequestBeneficiaryType.SELF
                : request.submissionDetails().beneficiaryType();
        java.util.Set<AttachmentDocumentType> uploaded = attachmentStore.findByRequestId(request.id()).stream()
                .map(PassportRequestAttachment::documentType)
                .collect(java.util.stream.Collectors.toSet());
        return AttachmentRequirements.from(
                AttachmentRequirementPolicy.forPassport(request.kind(), beneficiaryType), uploaded);
    }

    private PassportRequest citizenOwnedRequest(CitizenId citizenId, UUID requestId) {
        PassportRequest request = requireRequest(requestId);
        if (!request.belongsTo(citizenId)) {
            throw new NotFoundException("Request not found");
        }
        return request;
    }

    private PassportRequest requireRequest(UUID requestId) {
        return requestStore.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
    }

    private AttachmentContent content(UUID requestId, UUID attachmentId) {
        PassportRequestAttachment attachment = attachmentStore.findByIdAndRequestId(attachmentId, requestId)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
        return new AttachmentContent(attachment, contentStore.read(attachment.storageKey()));
    }

}
