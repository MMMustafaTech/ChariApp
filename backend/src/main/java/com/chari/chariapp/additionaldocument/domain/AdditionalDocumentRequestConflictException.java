package com.chari.chariapp.additionaldocument.domain;

public class AdditionalDocumentRequestConflictException extends RuntimeException {
    private final String code;
    public AdditionalDocumentRequestConflictException(String code) { super(code); this.code = code; }
    public String code() { return code; }
}
