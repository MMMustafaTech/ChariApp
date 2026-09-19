package com.chari.chariapp.birthrequest.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.birthrequest.application.port.out.*;
import com.chari.chariapp.birthrequest.domain.*;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.application.DocumentNotFoundException;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.UUID;

public class SubmitBirthCertificateRequestService {
    private final PassportRequestActorAccess access; private final BirthCertificateRequestStore requests; private final BirthCertificateRequestStatusHistoryStore history; private final OperationalAuditStore audit; private final Clock clock;
    private final NewbornRegistrationDetailsStore newbornDetails;
    private final CitizenDocumentReadStore documentStore;
    public SubmitBirthCertificateRequestService(PassportRequestActorAccess access, BirthCertificateRequestStore requests, BirthCertificateRequestStatusHistoryStore history, NewbornRegistrationDetailsStore newbornDetails, CitizenDocumentReadStore documentStore, OperationalAuditStore audit, Clock clock) { this.access = access; this.requests = requests; this.history = history; this.newbornDetails = newbornDetails; this.documentStore = documentStore; this.audit = audit; this.clock = clock; }
    @Transactional public BirthCertificateRequest submit(AccountId actorId, BirthCertificateRequestKind kind, String reason, NewbornRegistrationDetails details) {
        if (kind == BirthCertificateRequestKind.NEWBORN_REGISTRATION && details == null) throw new IllegalArgumentException("Newborn registration details are required");
        if (kind != BirthCertificateRequestKind.NEWBORN_REGISTRATION && details != null) throw new IllegalArgumentException("Newborn details are only valid for a newborn registration");
        CitizenId citizenId = access.requireActiveCitizen(actorId);
        if (kind != BirthCertificateRequestKind.NEWBORN_REGISTRATION
                && documentStore.findBirthCertificateByCitizenId(citizenId).isEmpty()) {
            throw new DocumentNotFoundException();
        }
        if (requests.hasOpenRequest(citizenId, kind)) throw new BirthCertificateRequestConflictException(BirthCertificateRequestConflictException.Reason.OPEN_REQUEST_EXISTS);
        Instant now = Instant.now(clock); BirthCertificateRequest request = BirthCertificateRequest.submitted(citizenId, kind, reason, now);
        try { requests.save(request); } catch (DataIntegrityViolationException ex) { throw new BirthCertificateRequestConflictException(BirthCertificateRequestConflictException.Reason.OPEN_REQUEST_EXISTS); }
        if (details != null) newbornDetails.save(request.id(), details);
        history.append(new BirthCertificateRequestStatusChange(UUID.randomUUID(), request.id(), null, BirthCertificateRequestStatus.SUBMITTED, null, actorId, now));
        audit.record(actorId.value().toString(), "BIRTH_CERTIFICATE_REQUEST_SUBMITTED", "SERVICE_REQUEST", request.id().toString(), "kind=" + kind, now);
        return request;
    }
}
