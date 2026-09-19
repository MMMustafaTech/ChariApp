package com.chari.chariapp.identityrequest.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.application.DocumentAlreadyExistsException;
import com.chari.chariapp.document.application.DocumentNotFoundException;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStatusHistoryStore;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStore;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequest;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestKind;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestStatus;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestStatusChange;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.request.domain.RequestBeneficiaryType;
import com.chari.chariapp.request.domain.ServiceRequestSubmissionDetails;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class SubmitNationalIdentityRequestService {
    private final PassportRequestActorAccess actorAccess;
    private final NationalIdentityRequestStore requestStore;
    private final NationalIdentityRequestStatusHistoryStore historyStore;
    private final CitizenDocumentReadStore documentStore;
    private final OperationalAuditStore auditStore;
    private final Clock clock;

    public SubmitNationalIdentityRequestService(PassportRequestActorAccess actorAccess, NationalIdentityRequestStore requestStore,
                                                NationalIdentityRequestStatusHistoryStore historyStore, CitizenDocumentReadStore documentStore,
                                                OperationalAuditStore auditStore,
                                                Clock clock) {
        this.actorAccess = actorAccess;
        this.requestStore = requestStore;
        this.historyStore = historyStore;
        this.documentStore = documentStore;
        this.auditStore = auditStore;
        this.clock = clock;
    }

    @Transactional
    public NationalIdentityRequest submit(AccountId actorId, NationalIdentityRequestKind kind, String reason) {
        return submitInternal(actorId, kind, reason, null);
    }

    @Transactional
    public NationalIdentityRequest submit(AccountId actorId, NationalIdentityRequestKind kind, String reason,
                                          ServiceRequestSubmissionDetails details) {
        if (details == null) throw new IllegalArgumentException("Submission details are required");
        return submitInternal(actorId, kind, reason, details);
    }

    private NationalIdentityRequest submitInternal(AccountId actorId, NationalIdentityRequestKind kind, String reason,
                                                   ServiceRequestSubmissionDetails details) {
        CitizenId citizenId = actorAccess.requireActiveCitizen(actorId);
        validateDetails(citizenId, kind, details);
        boolean dependentChild = details != null
                && details.beneficiaryType() == RequestBeneficiaryType.DEPENDENT_CHILD;
        boolean documentExists = !dependentChild && documentStore.findNationalIdentityByCitizenId(citizenId).isPresent();
        if (kind == NationalIdentityRequestKind.ISSUANCE && documentExists) {
            throw new DocumentAlreadyExistsException();
        }
        if (kind != NationalIdentityRequestKind.ISSUANCE && !documentExists) {
            throw new DocumentNotFoundException();
        }
        if (requestStore.hasOpenRequest(citizenId)) {
            throw new NationalIdentityRequestConflictException(NationalIdentityRequestConflictException.Reason.OPEN_REQUEST_EXISTS);
        }
        Instant now = Instant.now(clock);
        NationalIdentityRequest request = NationalIdentityRequest.submitted(citizenId, kind, reason, details, now);
        try {
            requestStore.save(request);
        } catch (DataIntegrityViolationException exception) {
            throw new NationalIdentityRequestConflictException(NationalIdentityRequestConflictException.Reason.OPEN_REQUEST_EXISTS);
        }
        historyStore.append(new NationalIdentityRequestStatusChange(UUID.randomUUID(), request.id(), null,
                NationalIdentityRequestStatus.SUBMITTED, null, actorId, now));
        auditStore.record(actorId.value().toString(), "NATIONAL_IDENTITY_REQUEST_SUBMITTED", "SERVICE_REQUEST",
                request.id().toString(), "kind=" + kind, now);
        return request;
    }

    private void validateDetails(CitizenId citizenId, NationalIdentityRequestKind kind,
                                 ServiceRequestSubmissionDetails details) {
        if (details == null) return;
        if (details.beneficiaryType() == RequestBeneficiaryType.DEPENDENT_CHILD) {
            if (kind != NationalIdentityRequestKind.ISSUANCE) {
                throw new IllegalArgumentException("A dependent child request only supports first issuance");
            }
            boolean owned = documentStore.findDependentBirthCertificateById(details.dependentBirthCertificateId())
                    .filter(value -> value.parentCitizenId().equals(citizenId))
                    .isPresent();
            if (!owned) throw new NotFoundException("Dependent birth certificate not found");
        }
        if (kind == NationalIdentityRequestKind.LOST && details.lossReportNumber() == null) {
            throw new IllegalArgumentException("A lost identity request requires a loss report number");
        }
        if (kind != NationalIdentityRequestKind.LOST && details.lossReportNumber() != null) {
            throw new IllegalArgumentException("Loss report number is only valid for a lost identity");
        }
    }
}
