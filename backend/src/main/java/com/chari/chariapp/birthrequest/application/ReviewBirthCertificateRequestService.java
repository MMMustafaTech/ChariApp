package com.chari.chariapp.birthrequest.application;

import com.chari.chariapp.account.domain.*;
import com.chari.chariapp.additionaldocument.application.port.out.AdditionalDocumentRequestStore;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequestConflictException;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.birthrequest.application.port.out.*;
import com.chari.chariapp.birthrequest.domain.*;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.notification.domain.NotificationType;
import com.chari.chariapp.document.application.DocumentIssuanceService;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.UUID;

public class ReviewBirthCertificateRequestService {
    private final PassportRequestActorAccess access; private final BirthCertificateRequestStore requests; private final BirthCertificateRequestStatusHistoryStore history; private final OperationalAuditStore audit; private final NotificationService notifications; private final AdditionalDocumentRequestStore additionalDocuments; private final DocumentIssuanceService documentIssuance; private final Clock clock;
    public ReviewBirthCertificateRequestService(PassportRequestActorAccess access, BirthCertificateRequestStore requests, BirthCertificateRequestStatusHistoryStore history, OperationalAuditStore audit, NotificationService notifications, AdditionalDocumentRequestStore additionalDocuments, DocumentIssuanceService documentIssuance, Clock clock) { this.access = access; this.requests = requests; this.history = history; this.audit = audit; this.notifications = notifications; this.additionalDocuments = additionalDocuments; this.documentIssuance = documentIssuance; this.clock = clock; }
    @Transactional public BirthCertificateRequest startReview(AccountId actorId, UUID requestId) { Account operator = access.requireActiveOperator(actorId); BirthCertificateRequest current = find(requestId); rejectSelfReview(operator, current); Instant now = Instant.now(clock); BirthCertificateRequest updated = current.startReview(actorId, now); requests.save(updated); history(updated, current, null, actorId, now); audit.record(actorId.value().toString(), "BIRTH_CERTIFICATE_REQUEST_REVIEW_STARTED", "SERVICE_REQUEST", requestId.toString(), null, now); return updated; }
    @Transactional public BirthCertificateRequest decide(AccountId actorId, UUID requestId, boolean approved, String reason) { Account operator = access.requireActiveOperator(actorId); BirthCertificateRequest current = find(requestId); rejectSelfReview(operator, current); if(additionalDocuments.hasOpenRequest(ServiceRequestType.BIRTH_CERTIFICATE,requestId)) throw new AdditionalDocumentRequestConflictException("ADDITIONAL_DOCUMENTS_UNRESOLVED"); Instant now = Instant.now(clock); BirthCertificateRequest updated = current.decide(actorId, approved, reason, now); if(approved) documentIssuance.issue(current,actorId,now); requests.save(updated); history(updated, current, updated.decisionReason(), actorId, now); audit.record(actorId.value().toString(), approved ? "BIRTH_CERTIFICATE_REQUEST_APPROVED" : "BIRTH_CERTIFICATE_REQUEST_REJECTED", "SERVICE_REQUEST", requestId.toString(), null, now); notifications.publish(updated.citizenId(), approved ? NotificationType.BIRTH_CERTIFICATE_REQUEST_APPROVED : NotificationType.BIRTH_CERTIFICATE_REQUEST_REJECTED, approved ? "Birth certificate request approved" : "Birth certificate request rejected", approved ? "Your birth certificate request has been approved." : "Your birth certificate request has been rejected."); return updated; }
    private BirthCertificateRequest find(UUID id) { return requests.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Request not found")); }
    private void rejectSelfReview(Account operator, BirthCertificateRequest request) { if (operator.citizenIdOptional().filter(request.citizenId()::equals).isPresent()) throw new BirthCertificateRequestConflictException(BirthCertificateRequestConflictException.Reason.SELF_REVIEW_NOT_ALLOWED); }
    private void history(BirthCertificateRequest updated, BirthCertificateRequest previous, String reason, AccountId actor, Instant at) { history.append(new BirthCertificateRequestStatusChange(UUID.randomUUID(), updated.id(), previous.status(), updated.status(), reason, actor, at)); }
}
