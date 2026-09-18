package com.chari.chariapp.birthrequest.domain;

public class BirthCertificateRequestTransitionException extends RuntimeException {
    public enum Reason {
        NOT_AWAITING_REVIEW,
        NOT_UNDER_REVIEW,
        REVIEWER_MISMATCH,
        REJECTION_REASON_REQUIRED
    }

    private final Reason reason;

    public BirthCertificateRequestTransitionException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() { return reason; }
}
