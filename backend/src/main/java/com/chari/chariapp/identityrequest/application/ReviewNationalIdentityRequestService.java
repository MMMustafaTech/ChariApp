package com.chari.chariapp.identityrequest.application;

import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.additionaldocument.application.port.out.AdditionalDocumentRequestStore;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequestConflictException;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStatusHistoryStore;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStore;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequest;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestStatusChange;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.notification.domain.NotificationType;
import com.chari.chariapp.document.application.DocumentIssuanceService;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class ReviewNationalIdentityRequestService {
    private final PassportRequestActorAccess actorAccess;
    private final NationalIdentityRequestStore requestStore;
    private final NationalIdentityRequestStatusHistoryStore historyStore;
    private final OperationalAuditStore auditStore;
    private final NotificationService notifications;
    private final AdditionalDocumentRequestStore additionalDocuments;
    private final DocumentIssuanceService documentIssuance;
    private final NationalIdentityRequestAttachmentService attachments;
    private final Clock clock;

    public ReviewNationalIdentityRequestService(PassportRequestActorAccess actorAccess, NationalIdentityRequestStore requestStore,
                                                NationalIdentityRequestStatusHistoryStore historyStore, OperationalAuditStore auditStore, NotificationService notifications,
                                                AdditionalDocumentRequestStore additionalDocuments,
                                                DocumentIssuanceService documentIssuance,
                                                NationalIdentityRequestAttachmentService attachments,
                                                Clock clock) {
        this.actorAccess = actorAccess;
        this.requestStore = requestStore;
        this.historyStore = historyStore;
        this.auditStore = auditStore;
        this.notifications = notifications;
        this.additionalDocuments = additionalDocuments;
        this.documentIssuance = documentIssuance;
        this.attachments = attachments;
        this.clock = clock;
    }

    @Transactional
    public NationalIdentityRequest startReview(AccountId actorId, UUID requestId) {
        Account operator = actorAccess.requireActiveOperator(actorId);
        NationalIdentityRequest current = findForUpdate(requestId);
        rejectSelfReview(operator, current);
        attachments.requireComplete(current);
        Instant now = Instant.now(clock);
        NationalIdentityRequest updated = current.startReview(actorId, now);
        requestStore.save(updated);
        appendHistory(updated, current, null, actorId, now);
        auditStore.record(actorId.value().toString(), "NATIONAL_IDENTITY_REQUEST_REVIEW_STARTED", "SERVICE_REQUEST",
                requestId.toString(), null, now);
        return updated;
    }

    @Transactional
    public NationalIdentityRequest decide(AccountId actorId, UUID requestId, boolean approved, String reason) {
        Account operator = actorAccess.requireActiveOperator(actorId);
        NationalIdentityRequest current = findForUpdate(requestId);
        rejectSelfReview(operator, current);
        if (additionalDocuments.hasOpenRequest(ServiceRequestType.NATIONAL_IDENTITY, requestId)) {
            throw new AdditionalDocumentRequestConflictException("ADDITIONAL_DOCUMENTS_UNRESOLVED");
        }
        Instant now = Instant.now(clock);
        NationalIdentityRequest updated = current.decide(actorId, approved, reason, now);
        if (approved) {
            documentIssuance.issue(current, actorId, now);
        }
        requestStore.save(updated);
        appendHistory(updated, current, updated.decisionReason(), actorId, now);
        auditStore.record(actorId.value().toString(), approved ? "NATIONAL_IDENTITY_REQUEST_APPROVED" : "NATIONAL_IDENTITY_REQUEST_REJECTED",
                "SERVICE_REQUEST", requestId.toString(), null, now);
        notifications.publish(updated.citizenId(), approved ? NotificationType.NATIONAL_IDENTITY_REQUEST_APPROVED : NotificationType.NATIONAL_IDENTITY_REQUEST_REJECTED,
                approved ? "National identity request approved" : "National identity request rejected",
                approved ? "Your national identity request has been approved." : "Your national identity request has been rejected.");
        return updated;
    }

    private NationalIdentityRequest findForUpdate(UUID requestId) {
        return requestStore.findByIdForUpdate(requestId).orElseThrow(() -> new NotFoundException("Request not found"));
    }

    private void rejectSelfReview(Account operator, NationalIdentityRequest request) {
        if (operator.citizenIdOptional().filter(request.citizenId()::equals).isPresent()) {
            throw new NationalIdentityRequestConflictException(NationalIdentityRequestConflictException.Reason.SELF_REVIEW_NOT_ALLOWED);
        }
    }

    private void appendHistory(NationalIdentityRequest updated, NationalIdentityRequest previous, String reason,
                               AccountId actorId, Instant occurredAt) {
        historyStore.append(new NationalIdentityRequestStatusChange(UUID.randomUUID(), updated.id(), previous.status(),
                updated.status(), reason, actorId, occurredAt));
    }
}
