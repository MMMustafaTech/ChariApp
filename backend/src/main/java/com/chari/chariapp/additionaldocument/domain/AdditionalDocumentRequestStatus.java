package com.chari.chariapp.additionaldocument.domain;

public enum AdditionalDocumentRequestStatus {
    REQUESTED,
    SUBMITTED,
    RESOLVED;

    public boolean isOpen() { return this != RESOLVED; }
}
