package com.chari.chariapp.birthrequest.application;

public class BirthCertificateRequestConflictException extends RuntimeException {
    public enum Reason { OPEN_REQUEST_EXISTS, ATTACHMENTS_CLOSED, SELF_REVIEW_NOT_ALLOWED }

    private final Reason reason;

    public BirthCertificateRequestConflictException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() { return reason; }
}
