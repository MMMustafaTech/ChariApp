package com.chari.chariapp.document.application;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException() {
        super("Document not found");
    }
}
