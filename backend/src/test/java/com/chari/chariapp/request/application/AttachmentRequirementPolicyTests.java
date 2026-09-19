package com.chari.chariapp.request.application;

import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestKind;
import com.chari.chariapp.request.domain.AttachmentDocumentType;
import com.chari.chariapp.request.domain.PassportRequestKind;
import com.chari.chariapp.request.domain.RequestBeneficiaryType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentRequirementPolicyTests {

    @Test
    void firstIssuanceRequiresTheFiveOfficialDocumentTypes() {
        assertThat(AttachmentRequirementPolicy.forPassport(
                PassportRequestKind.ISSUANCE, RequestBeneficiaryType.SELF))
                .containsExactlyInAnyOrder(
                        AttachmentDocumentType.POPULATION_REGISTRY_EXTRACT,
                        AttachmentDocumentType.BIRTH_CERTIFICATE_COPY,
                        AttachmentDocumentType.PERSONAL_PHOTO,
                        AttachmentDocumentType.PROFESSION_PROOF,
                        AttachmentDocumentType.PAYMENT_RECEIPT
                );
    }

    @Test
    void lostIdentityRequiresTheLossReportInsteadOfAnOldDocument() {
        Set<AttachmentDocumentType> required = AttachmentRequirementPolicy.forNationalIdentity(
                NationalIdentityRequestKind.LOST, RequestBeneficiaryType.SELF);

        assertThat(required).contains(AttachmentDocumentType.LOSS_OR_THEFT_REPORT);
        assertThat(required).doesNotContain(AttachmentDocumentType.OLD_DOCUMENT);
    }

    @Test
    void dependentChildDoesNotRequireProfessionProof() {
        assertThat(AttachmentRequirementPolicy.forPassport(
                PassportRequestKind.ISSUANCE, RequestBeneficiaryType.DEPENDENT_CHILD))
                .containsExactlyInAnyOrder(
                        AttachmentDocumentType.POPULATION_REGISTRY_EXTRACT,
                        AttachmentDocumentType.BIRTH_CERTIFICATE_COPY,
                        AttachmentDocumentType.PERSONAL_PHOTO,
                        AttachmentDocumentType.PAYMENT_RECEIPT
                );
    }

    @Test
    void requirementStatusReportsUploadedAndMissingTypes() {
        AttachmentRequirements status = AttachmentRequirements.from(
                Set.of(AttachmentDocumentType.PERSONAL_PHOTO, AttachmentDocumentType.PAYMENT_RECEIPT),
                Set.of(AttachmentDocumentType.PERSONAL_PHOTO)
        );

        assertThat(status.complete()).isFalse();
        assertThat(status.missing()).containsExactly(AttachmentDocumentType.PAYMENT_RECEIPT);
    }
}
