package com.chari.chariapp.request.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.application.DocumentAlreadyExistsException;
import com.chari.chariapp.document.application.DocumentNotFoundException;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.request.application.port.out.PassportRequestStatusHistoryStore;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.request.domain.PassportRequest;
import com.chari.chariapp.request.domain.PassportRequestStatus;
import com.chari.chariapp.request.domain.PassportRequestKind;
import com.chari.chariapp.request.domain.PassportRequestStatusChange;
import com.chari.chariapp.request.domain.RequestBeneficiaryType;
import com.chari.chariapp.request.domain.ServiceRequestSubmissionDetails;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.settings.application.SystemSettingsService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/** Handles the citizen command that creates a new passport request. */
public class SubmitPassportRequestService {

    private final PassportRequestActorAccess actorAccess;
    private final PassportRequestStore requestStore;
    private final PassportRequestStatusHistoryStore historyStore;
    private final CitizenDocumentReadStore documentStore;
    private final OperationalAuditStore auditStore;
    private final SystemSettingsService settings;
    private final Clock clock;

    public SubmitPassportRequestService(
            PassportRequestActorAccess actorAccess,
            PassportRequestStore requestStore,
            PassportRequestStatusHistoryStore historyStore,
            CitizenDocumentReadStore documentStore,
            OperationalAuditStore auditStore,
            SystemSettingsService settings,
            Clock clock
    ) {
        this.actorAccess = actorAccess;
        this.requestStore = requestStore;
        this.historyStore = historyStore;
        this.documentStore = documentStore;
        this.auditStore = auditStore;
        this.settings = settings;
        this.clock = clock;
    }

    @Transactional
    public PassportRequest submit(AccountId actorId) {
        return submit(actorId, PassportRequestKind.ISSUANCE, null);
    }

    @Transactional
    public PassportRequest submit(AccountId actorId, PassportRequestKind kind, String requestReason) {
        return submitInternal(actorId, kind, requestReason, null);
    }

    @Transactional
    public PassportRequest submit(AccountId actorId, PassportRequestKind kind, String requestReason,
                                  ServiceRequestSubmissionDetails details) {
        if (details == null) throw new IllegalArgumentException("Submission details are required");
        return submitInternal(actorId, kind, requestReason, details);
    }

    private PassportRequest submitInternal(AccountId actorId, PassportRequestKind kind, String requestReason,
                                           ServiceRequestSubmissionDetails details) {
        settings.requireRequestSubmissionsEnabled();
        CitizenId citizenId = actorAccess.requireActiveCitizen(actorId);
        validateDetails(citizenId, kind, details);
        boolean dependentChild = details != null
                && details.beneficiaryType() == RequestBeneficiaryType.DEPENDENT_CHILD;
        boolean documentExists = !dependentChild && documentStore.findPassportByCitizenId(citizenId).isPresent();
        if (kind == PassportRequestKind.ISSUANCE && documentExists) {
            throw new DocumentAlreadyExistsException();
        }
        if (kind != PassportRequestKind.ISSUANCE && !documentExists) {
            throw new DocumentNotFoundException();
        }
        if (requestStore.hasOpenRequest(citizenId)) {
            throw new PassportRequestConflictException(PassportRequestConflictException.Reason.OPEN_REQUEST_EXISTS);
        }

        Instant submittedAt = Instant.now(clock);
        PassportRequest request = PassportRequest.submitted(citizenId, kind, requestReason, details, submittedAt);

        try {
            requestStore.save(request);
        } catch (DataIntegrityViolationException exception) {
            // A database-level uniqueness rule can be added later without leaking its details to callers.
            throw new PassportRequestConflictException(PassportRequestConflictException.Reason.OPEN_REQUEST_EXISTS);
        }

        historyStore.append(new PassportRequestStatusChange(
                UUID.randomUUID(), request.id(), null, PassportRequestStatus.SUBMITTED,
                null, actorId, submittedAt
        ));
        auditStore.record(
                actorId.value().toString(), "PASSPORT_REQUEST_SUBMITTED", "SERVICE_REQUEST",
                request.id().toString(), "kind=" + request.kind(), submittedAt
        );
        return request;
    }

    private void validateDetails(CitizenId citizenId, PassportRequestKind kind,
                                 ServiceRequestSubmissionDetails details) {
        if (details == null) return;
        if (details.beneficiaryType() == RequestBeneficiaryType.DEPENDENT_CHILD) {
            if (kind != PassportRequestKind.ISSUANCE) {
                throw new IllegalArgumentException("A dependent child request only supports first issuance");
            }
            boolean owned = documentStore.findDependentBirthCertificateById(details.dependentBirthCertificateId())
                    .filter(value -> value.parentCitizenId().equals(citizenId))
                    .isPresent();
            if (!owned) throw new NotFoundException("Dependent birth certificate not found");
        }
        if (kind == PassportRequestKind.LOST && details.lossReportNumber() == null) {
            throw new IllegalArgumentException("A lost passport request requires a loss report number");
        }
        if (kind != PassportRequestKind.LOST && details.lossReportNumber() != null) {
            throw new IllegalArgumentException("Loss report number is only valid for a lost passport");
        }
    }
}
