package com.chari.chariapp.document.application;

public class DocumentAlreadyExistsException extends RuntimeException {
    public DocumentAlreadyExistsException() {
        super("Document already exists");
    }
}
