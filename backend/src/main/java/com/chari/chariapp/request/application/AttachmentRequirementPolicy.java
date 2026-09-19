package com.chari.chariapp.request.application;

import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestKind;
import com.chari.chariapp.request.domain.AttachmentDocumentType;
import com.chari.chariapp.request.domain.PassportRequestKind;
import com.chari.chariapp.request.domain.RequestBeneficiaryType;

import java.util.EnumSet;
import java.util.Set;

public final class AttachmentRequirementPolicy {
    private AttachmentRequirementPolicy() {
    }

    public static Set<AttachmentDocumentType> forPassport(
            PassportRequestKind kind,
            RequestBeneficiaryType beneficiaryType
    ) {
        return forIdentityOrPassport(kind.name(), beneficiaryType);
    }

    public static Set<AttachmentDocumentType> forNationalIdentity(
            NationalIdentityRequestKind kind,
            RequestBeneficiaryType beneficiaryType
    ) {
        return forIdentityOrPassport(kind.name(), beneficiaryType);
    }

    private static Set<AttachmentDocumentType> forIdentityOrPassport(
            String kind,
            RequestBeneficiaryType beneficiaryType
    ) {
        if (beneficiaryType == RequestBeneficiaryType.DEPENDENT_CHILD) {
            return Set.of(
                    AttachmentDocumentType.POPULATION_REGISTRY_EXTRACT,
                    AttachmentDocumentType.BIRTH_CERTIFICATE_COPY,
                    AttachmentDocumentType.PERSONAL_PHOTO,
                    AttachmentDocumentType.PAYMENT_RECEIPT
            );
        }

        EnumSet<AttachmentDocumentType> required = EnumSet.of(
                AttachmentDocumentType.BIRTH_CERTIFICATE_COPY,
                AttachmentDocumentType.PERSONAL_PHOTO,
                AttachmentDocumentType.PROFESSION_PROOF,
                AttachmentDocumentType.PAYMENT_RECEIPT
        );
        switch (kind) {
            case "ISSUANCE" -> required.add(AttachmentDocumentType.POPULATION_REGISTRY_EXTRACT);
            case "RENEWAL", "DAMAGED" -> required.add(AttachmentDocumentType.OLD_DOCUMENT);
            case "LOST" -> required.add(AttachmentDocumentType.LOSS_OR_THEFT_REPORT);
            case "DATA_CORRECTION" -> required.add(AttachmentDocumentType.DATA_CORRECTION_PROOF);
            default -> throw new IllegalArgumentException("Unsupported request kind: " + kind);
        }
        return Set.copyOf(required);
    }
}
