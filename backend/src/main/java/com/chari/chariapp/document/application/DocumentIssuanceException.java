package com.chari.chariapp.document.application;

public class DocumentIssuanceException extends RuntimeException {
    private final String code;

    public DocumentIssuanceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
