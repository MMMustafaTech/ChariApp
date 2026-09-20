package com.chari.chariapp.exception;

public class FeatureDisabledException extends RuntimeException {
    private final String code;

    public FeatureDisabledException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
