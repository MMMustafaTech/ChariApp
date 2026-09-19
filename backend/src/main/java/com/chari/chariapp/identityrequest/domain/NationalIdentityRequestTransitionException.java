package com.chari.chariapp.identityrequest.domain;

public class NationalIdentityRequestTransitionException extends RuntimeException {
    public enum Reason { NOT_AWAITING_REVIEW, NOT_UNDER_REVIEW, REVIEWER_MISMATCH, REJECTION_REASON_REQUIRED }

    private final Reason reason;

    public NationalIdentityRequestTransitionException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() { return reason; }
}
