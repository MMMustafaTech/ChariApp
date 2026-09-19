package com.chari.chariapp.request.application;

import com.chari.chariapp.request.domain.AttachmentDocumentType;

import java.util.Set;

public record AttachmentRequirements(
        Set<AttachmentDocumentType> required,
        Set<AttachmentDocumentType> uploaded,
        Set<AttachmentDocumentType> missing,
        boolean complete
) {
    public static AttachmentRequirements from(
            Set<AttachmentDocumentType> required,
            Set<AttachmentDocumentType> uploaded
    ) {
        Set<AttachmentDocumentType> requiredCopy = Set.copyOf(required);
        Set<AttachmentDocumentType> uploadedCopy = Set.copyOf(uploaded);
        java.util.EnumSet<AttachmentDocumentType> missing = requiredCopy.isEmpty()
                ? java.util.EnumSet.noneOf(AttachmentDocumentType.class)
                : java.util.EnumSet.copyOf(requiredCopy);
        missing.removeAll(uploadedCopy);
        return new AttachmentRequirements(requiredCopy, uploadedCopy, Set.copyOf(missing), missing.isEmpty());
    }
}
