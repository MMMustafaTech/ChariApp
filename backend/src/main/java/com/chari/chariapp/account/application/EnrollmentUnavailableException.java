package com.chari.chariapp.account.application;

public class EnrollmentUnavailableException extends RuntimeException {

    public enum Reason {
        NATIONAL_ID_NOT_FOUND,
        CITIZEN_ACCOUNT_EXISTS,
        EMAIL_ALREADY_EXISTS
    }

    private final Reason reason;

    public EnrollmentUnavailableException(Reason reason) {
        super("Enrollment unavailable: " + reason);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
