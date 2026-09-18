package com.chari.chariapp.birthrequest.domain;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record BirthCertificateRequest(UUID id, CitizenId citizenId, BirthCertificateRequestKind kind, String requestReason,
                                      BirthCertificateRequestStatus status, AccountId reviewedBy, Instant submittedAt,
                                      Instant reviewedAt, String decisionReason) {
    public BirthCertificateRequest {
        Objects.requireNonNull(id); Objects.requireNonNull(citizenId); Objects.requireNonNull(kind); Objects.requireNonNull(status); Objects.requireNonNull(submittedAt);
        requestReason = normalize(requestReason); decisionReason = normalize(decisionReason);
        if (requestReason != null && requestReason.length() > 1000) throw new IllegalArgumentException("Request reason must not exceed 1000 characters");
        if (kind.requiresReason() && requestReason == null) throw new IllegalArgumentException("This birth certificate request type requires a reason");
        if (status == BirthCertificateRequestStatus.SUBMITTED && (reviewedBy != null || reviewedAt != null || decisionReason != null)) throw new IllegalArgumentException("A submitted request cannot contain review data");
        if (status != BirthCertificateRequestStatus.SUBMITTED && (reviewedBy == null || reviewedAt == null)) throw new IllegalArgumentException("A reviewed request must identify its reviewer and review time");
        if (status == BirthCertificateRequestStatus.UNDER_REVIEW && decisionReason != null) throw new IllegalArgumentException("An undecided request cannot contain a decision reason");
        if (status == BirthCertificateRequestStatus.REJECTED && decisionReason == null) throw new IllegalArgumentException("A rejected request requires a reason");
    }
    public static BirthCertificateRequest submitted(CitizenId citizenId, BirthCertificateRequestKind kind, String reason, Instant now) { return new BirthCertificateRequest(UUID.randomUUID(), citizenId, kind, reason, BirthCertificateRequestStatus.SUBMITTED, null, now, null, null); }
    public BirthCertificateRequest startReview(AccountId operator, Instant now) { if (status != BirthCertificateRequestStatus.SUBMITTED) throw new BirthCertificateRequestTransitionException(BirthCertificateRequestTransitionException.Reason.NOT_AWAITING_REVIEW); return new BirthCertificateRequest(id, citizenId, kind, requestReason, BirthCertificateRequestStatus.UNDER_REVIEW, operator, submittedAt, now, null); }
    public BirthCertificateRequest decide(AccountId operator, boolean approved, String reason, Instant now) { if (status != BirthCertificateRequestStatus.UNDER_REVIEW) throw new BirthCertificateRequestTransitionException(BirthCertificateRequestTransitionException.Reason.NOT_UNDER_REVIEW); if (!operator.equals(reviewedBy)) throw new BirthCertificateRequestTransitionException(BirthCertificateRequestTransitionException.Reason.REVIEWER_MISMATCH); if (!approved && normalize(reason) == null) throw new BirthCertificateRequestTransitionException(BirthCertificateRequestTransitionException.Reason.REJECTION_REASON_REQUIRED); return new BirthCertificateRequest(id, citizenId, kind, requestReason, approved ? BirthCertificateRequestStatus.APPROVED : BirthCertificateRequestStatus.REJECTED, operator, submittedAt, now, reason); }
    public boolean belongsTo(CitizenId candidate) { return citizenId.equals(candidate); }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
