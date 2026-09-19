package com.chari.chariapp.identityrequest.application;

public class NationalIdentityRequestConflictException extends RuntimeException {
    public enum Reason { OPEN_REQUEST_EXISTS, ATTACHMENTS_CLOSED, SELF_REVIEW_NOT_ALLOWED, MISSING_REQUIRED_ATTACHMENTS }

    private final Reason reason;

    public NationalIdentityRequestConflictException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() { return reason; }
}
