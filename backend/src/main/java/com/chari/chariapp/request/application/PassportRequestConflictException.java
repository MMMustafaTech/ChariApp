package com.chari.chariapp.request.application;

/** A client-safe conflict with the current passport request workflow state. */
public class PassportRequestConflictException extends RuntimeException {
    public enum Reason { OPEN_REQUEST_EXISTS, ATTACHMENTS_CLOSED, SELF_REVIEW_NOT_ALLOWED, MISSING_REQUIRED_ATTACHMENTS }

    private final Reason reason;

    public PassportRequestConflictException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() { return reason; }
}
