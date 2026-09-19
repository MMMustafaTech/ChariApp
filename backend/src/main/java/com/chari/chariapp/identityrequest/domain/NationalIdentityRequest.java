package com.chari.chariapp.identityrequest.domain;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.request.domain.ServiceRequestSubmissionDetails;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A citizen request for a national-identity service. */
public record NationalIdentityRequest(
        UUID id,
        CitizenId citizenId,
        NationalIdentityRequestKind kind,
        String requestReason,
        ServiceRequestSubmissionDetails submissionDetails,
        NationalIdentityRequestStatus status,
        AccountId reviewedBy,
        Instant submittedAt,
        Instant reviewedAt,
        String decisionReason
) {
    public NationalIdentityRequest {
        Objects.requireNonNull(id, "Request ID is required");
        Objects.requireNonNull(citizenId, "Citizen ID is required");
        Objects.requireNonNull(kind, "Request kind is required");
        Objects.requireNonNull(status, "Request status is required");
        Objects.requireNonNull(submittedAt, "Submission time is required");
        requestReason = normalizeReason(requestReason);
        decisionReason = normalizeReason(decisionReason);
        if (requestReason != null && requestReason.length() > 1000) {
            throw new IllegalArgumentException("Request reason must not exceed 1000 characters");
        }
        if (kind.requiresReason() && requestReason == null) {
            throw new IllegalArgumentException("This identity request type requires a reason");
        }
        if (status == NationalIdentityRequestStatus.SUBMITTED && (reviewedBy != null || reviewedAt != null || decisionReason != null)) {
            throw new IllegalArgumentException("A submitted request cannot contain review data");
        }
        if (status != NationalIdentityRequestStatus.SUBMITTED && (reviewedBy == null || reviewedAt == null)) {
            throw new IllegalArgumentException("A reviewed request must identify its reviewer and review time");
        }
        if (status == NationalIdentityRequestStatus.UNDER_REVIEW && decisionReason != null) {
            throw new IllegalArgumentException("An undecided request cannot contain a decision reason");
        }
        if (status == NationalIdentityRequestStatus.REJECTED && decisionReason == null) {
            throw new IllegalArgumentException("A rejected request requires a reason");
        }
    }

    public NationalIdentityRequest(UUID id, CitizenId citizenId, NationalIdentityRequestKind kind,
                                   String requestReason, NationalIdentityRequestStatus status,
                                   AccountId reviewedBy, Instant submittedAt, Instant reviewedAt,
                                   String decisionReason) {
        this(id, citizenId, kind, requestReason, null, status, reviewedBy, submittedAt, reviewedAt, decisionReason);
    }

    public static NationalIdentityRequest submitted(CitizenId citizenId, NationalIdentityRequestKind kind, String reason, Instant now) {
        return submitted(citizenId, kind, reason, null, now);
    }

    public static NationalIdentityRequest submitted(CitizenId citizenId, NationalIdentityRequestKind kind,
                                                     String reason, ServiceRequestSubmissionDetails submissionDetails,
                                                     Instant now) {
        return new NationalIdentityRequest(UUID.randomUUID(), citizenId, kind, reason, submissionDetails,
                NationalIdentityRequestStatus.SUBMITTED, null, now, null, null);
    }

    public NationalIdentityRequest startReview(AccountId operator, Instant now) {
        if (status != NationalIdentityRequestStatus.SUBMITTED) {
            throw new NationalIdentityRequestTransitionException(NationalIdentityRequestTransitionException.Reason.NOT_AWAITING_REVIEW);
        }
        return new NationalIdentityRequest(id, citizenId, kind, requestReason, submissionDetails,
                NationalIdentityRequestStatus.UNDER_REVIEW, operator, submittedAt, now, null);
    }

    public NationalIdentityRequest decide(AccountId operator, boolean approved, String reason, Instant now) {
        if (status != NationalIdentityRequestStatus.UNDER_REVIEW) {
            throw new NationalIdentityRequestTransitionException(NationalIdentityRequestTransitionException.Reason.NOT_UNDER_REVIEW);
        }
        if (!operator.equals(reviewedBy)) {
            throw new NationalIdentityRequestTransitionException(NationalIdentityRequestTransitionException.Reason.REVIEWER_MISMATCH);
        }
        if (!approved && normalizeReason(reason) == null) {
            throw new NationalIdentityRequestTransitionException(NationalIdentityRequestTransitionException.Reason.REJECTION_REASON_REQUIRED);
        }
        return new NationalIdentityRequest(id, citizenId, kind, requestReason, submissionDetails,
                approved ? NationalIdentityRequestStatus.APPROVED : NationalIdentityRequestStatus.REJECTED,
                operator, submittedAt, now, reason);
    }

    public boolean belongsTo(CitizenId candidate) {
        return citizenId.equals(candidate);
    }

    private static String normalizeReason(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
