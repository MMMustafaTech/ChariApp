package com.chari.chariapp.request.domain;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PassportRequest(
        UUID id,
        CitizenId citizenId,
        PassportRequestKind kind,
        String requestReason,
        ServiceRequestSubmissionDetails submissionDetails,
        PassportRequestStatus status,
        AccountId reviewedBy,
        Instant submittedAt,
        Instant reviewedAt,
        String decisionReason
) {

    public PassportRequest {
        Objects.requireNonNull(id, "Request ID is required");
        Objects.requireNonNull(citizenId, "Citizen ID is required");
        Objects.requireNonNull(kind, "Request kind is required");
        Objects.requireNonNull(status, "Request status is required");
        Objects.requireNonNull(submittedAt, "Submission time is required");

        requestReason = normalizeReason(requestReason);
        if (requestReason != null && requestReason.length() > 1000) {
            throw new IllegalArgumentException("Request reason must not exceed 1000 characters");
        }
        if (kind.requiresReason() && requestReason == null) {
            throw new IllegalArgumentException("This passport request type requires a reason");
        }
        decisionReason = normalizeReason(decisionReason);
        if (status == PassportRequestStatus.SUBMITTED && (reviewedBy != null || reviewedAt != null || decisionReason != null)) {
            throw new IllegalArgumentException("A submitted request cannot contain review data");
        }
        if (status != PassportRequestStatus.SUBMITTED && (reviewedBy == null || reviewedAt == null)) {
            throw new IllegalArgumentException("A reviewed request must identify its reviewer and review time");
        }
        if (status == PassportRequestStatus.UNDER_REVIEW && decisionReason != null) {
            throw new IllegalArgumentException("An undecided request cannot contain a decision reason");
        }
        if (status == PassportRequestStatus.REJECTED && decisionReason == null) {
            throw new IllegalArgumentException("A rejected request requires a reason");
        }
    }

    /** Compatibility constructor for existing issuance requests. */
    public PassportRequest(
            UUID id,
            CitizenId citizenId,
            PassportRequestKind kind,
            String requestReason,
            PassportRequestStatus status,
            AccountId reviewedBy,
            Instant submittedAt,
            Instant reviewedAt,
            String decisionReason
    ) {
        this(id, citizenId, kind, requestReason, null, status, reviewedBy, submittedAt, reviewedAt, decisionReason);
    }

    public PassportRequest(
            UUID id,
            CitizenId citizenId,
            PassportRequestStatus status,
            AccountId reviewedBy,
            Instant submittedAt,
            Instant reviewedAt,
            String decisionReason
    ) {
        this(id, citizenId, PassportRequestKind.ISSUANCE, null, null, status, reviewedBy, submittedAt, reviewedAt, decisionReason);
    }

    public static PassportRequest submitted(CitizenId citizenId, Instant now) {
        return submitted(citizenId, PassportRequestKind.ISSUANCE, null, now);
    }

    public static PassportRequest submitted(CitizenId citizenId, PassportRequestKind kind, String requestReason, Instant now) {
        return submitted(citizenId, kind, requestReason, null, now);
    }

    public static PassportRequest submitted(CitizenId citizenId, PassportRequestKind kind, String requestReason,
                                             ServiceRequestSubmissionDetails submissionDetails, Instant now) {
        return new PassportRequest(UUID.randomUUID(), citizenId, kind, requestReason, submissionDetails,
                PassportRequestStatus.SUBMITTED, null, now, null, null);
    }

    public PassportRequest startReview(AccountId operator, Instant now) {
        if (status != PassportRequestStatus.SUBMITTED) {
            throw new PassportRequestTransitionException(PassportRequestTransitionException.Reason.NOT_AWAITING_REVIEW);
        }
        return new PassportRequest(id, citizenId, kind, requestReason, submissionDetails,
                PassportRequestStatus.UNDER_REVIEW, operator, submittedAt, now, null);
    }

    public PassportRequest decide(AccountId operator, boolean approved, String reason, Instant now) {
        if (status != PassportRequestStatus.UNDER_REVIEW) {
            throw new PassportRequestTransitionException(PassportRequestTransitionException.Reason.NOT_UNDER_REVIEW);
        }
        if (!operator.equals(reviewedBy)) {
            throw new PassportRequestTransitionException(PassportRequestTransitionException.Reason.REVIEWER_MISMATCH);
        }
        if (!approved && normalizeReason(reason) == null) {
            throw new PassportRequestTransitionException(PassportRequestTransitionException.Reason.REJECTION_REASON_REQUIRED);
        }
        return new PassportRequest(id, citizenId, kind, requestReason, submissionDetails,
                approved ? PassportRequestStatus.APPROVED : PassportRequestStatus.REJECTED,
                operator, submittedAt, now, reason);
    }

    public boolean belongsTo(CitizenId candidateCitizenId) {
        return citizenId.equals(candidateCitizenId);
    }

    private static String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.trim();
    }
}
