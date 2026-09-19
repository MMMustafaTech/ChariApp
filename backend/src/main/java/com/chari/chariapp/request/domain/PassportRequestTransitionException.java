package com.chari.chariapp.request.domain;

/** Raised when a request is asked to make an invalid state transition. */
public class PassportRequestTransitionException extends RuntimeException {
    public enum Reason { NOT_AWAITING_REVIEW, NOT_UNDER_REVIEW, REVIEWER_MISMATCH, REJECTION_REASON_REQUIRED }

    private final Reason reason;

    public PassportRequestTransitionException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() { return reason; }
}
