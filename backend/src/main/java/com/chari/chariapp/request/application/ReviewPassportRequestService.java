package com.chari.chariapp.request.application;

import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.additionaldocument.application.port.out.AdditionalDocumentRequestStore;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequestConflictException;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.request.application.port.out.PassportRequestStatusHistoryStore;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.request.domain.PassportRequest;
import com.chari.chariapp.request.domain.PassportRequestStatusChange;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.notification.domain.NotificationType;
import com.chari.chariapp.document.application.DocumentIssuanceService;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/** Handles employee review and final decision commands. */
public class ReviewPassportRequestService {

    private final PassportRequestActorAccess actorAccess;
    private final PassportRequestStore requestStore;
    private final PassportRequestStatusHistoryStore historyStore;
    private final OperationalAuditStore auditStore;
    private final NotificationService notifications;
    private final AdditionalDocumentRequestStore additionalDocuments;
    private final DocumentIssuanceService documentIssuance;
    private final PassportRequestAttachmentService attachments;
    private final Clock clock;

    public ReviewPassportRequestService(
            PassportRequestActorAccess actorAccess,
            PassportRequestStore requestStore,
            PassportRequestStatusHistoryStore historyStore,
            OperationalAuditStore auditStore,
            NotificationService notifications,
            AdditionalDocumentRequestStore additionalDocuments,
            DocumentIssuanceService documentIssuance,
            PassportRequestAttachmentService attachments,
            Clock clock
    ) {
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
    public PassportRequest startReview(AccountId actorId, UUID requestId) {
        Account operator = actorAccess.requireActiveOperator(actorId);
        PassportRequest current = findForUpdate(requestId);
        rejectSelfReview(operator, current);
        attachments.requireComplete(current);
        Instant reviewedAt = Instant.now(clock);
        PassportRequest updated = current.startReview(actorId, reviewedAt);
        requestStore.save(updated);
        appendHistory(updated, current, null, actorId, reviewedAt);
        auditStore.record(
                actorId.value().toString(), "PASSPORT_REQUEST_REVIEW_STARTED", "SERVICE_REQUEST",
                requestId.toString(), null, reviewedAt
        );
        return updated;
    }

    @Transactional
    public PassportRequest decide(AccountId actorId, UUID requestId, boolean approved, String reason) {
        Account operator = actorAccess.requireActiveOperator(actorId);
        PassportRequest current = findForUpdate(requestId);
        rejectSelfReview(operator, current);
        if (additionalDocuments.hasOpenRequest(ServiceRequestType.PASSPORT, requestId)) {
            throw new AdditionalDocumentRequestConflictException("ADDITIONAL_DOCUMENTS_UNRESOLVED");
        }

        Instant decidedAt = Instant.now(clock);
        PassportRequest updated = current.decide(actorId, approved, reason, decidedAt);
        if (approved) {
            documentIssuance.issue(current, actorId, decidedAt);
        }
        requestStore.save(updated);
        appendHistory(updated, current, updated.decisionReason(), actorId, decidedAt);
        auditStore.record(
                actorId.value().toString(),
                approved ? "PASSPORT_REQUEST_APPROVED" : "PASSPORT_REQUEST_REJECTED",
                "SERVICE_REQUEST", requestId.toString(), null, decidedAt
        );
        notifications.publish(updated.citizenId(), approved ? NotificationType.PASSPORT_REQUEST_APPROVED : NotificationType.PASSPORT_REQUEST_REJECTED,
                approved ? "Passport request approved" : "Passport request rejected",
                approved ? "Your passport request has been approved." : "Your passport request has been rejected.");
        return updated;
    }

    private PassportRequest findForUpdate(UUID requestId) {
        return requestStore.findByIdForUpdate(requestId)
                .orElseThrow(() -> new com.chari.chariapp.exception.NotFoundException("Request not found"));
    }

    private void rejectSelfReview(Account operator, PassportRequest request) {
        if (operator.citizenIdOptional().filter(request.citizenId()::equals).isPresent()) {
            throw new PassportRequestConflictException(PassportRequestConflictException.Reason.SELF_REVIEW_NOT_ALLOWED);
        }
    }

    private void appendHistory(
            PassportRequest updated,
            PassportRequest previous,
            String reason,
            AccountId actorId,
            Instant occurredAt
    ) {
        historyStore.append(new PassportRequestStatusChange(
                UUID.randomUUID(), updated.id(), previous.status(), updated.status(),
                reason, actorId, occurredAt
        ));
    }
}
