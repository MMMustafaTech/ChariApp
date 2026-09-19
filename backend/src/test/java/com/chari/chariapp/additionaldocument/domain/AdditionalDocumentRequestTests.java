package com.chari.chariapp.additionaldocument.domain;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdditionalDocumentRequestTests {

    private static final Instant REQUESTED_AT = Instant.parse("2026-09-19T10:00:00Z");

    @Test
    void followsRequestedSubmittedResolvedLifecycle() {
        AccountId employee = AccountId.newId();
        AdditionalDocumentRequest requested = AdditionalDocumentRequest.create(
                ServiceRequestType.PASSPORT,
                java.util.UUID.randomUUID(),
                CitizenId.newId(),
                "Please upload the missing documents",
                Set.of("Birth certificate", "Personal photo"),
                employee,
                REQUESTED_AT
        );

        AdditionalDocumentRequest submitted = requested.submit(REQUESTED_AT.plusSeconds(60));
        AdditionalDocumentRequest resolved = submitted.resolve(employee, REQUESTED_AT.plusSeconds(120));

        assertThat(requested.status()).isEqualTo(AdditionalDocumentRequestStatus.REQUESTED);
        assertThat(submitted.status()).isEqualTo(AdditionalDocumentRequestStatus.SUBMITTED);
        assertThat(resolved.status()).isEqualTo(AdditionalDocumentRequestStatus.RESOLVED);
        assertThat(resolved.resolvedBy()).isEqualTo(employee);
    }

    @Test
    void cannotResolveDocumentsBeforeCitizenSubmitsThem() {
        AdditionalDocumentRequest requested = AdditionalDocumentRequest.create(
                ServiceRequestType.BIRTH_CERTIFICATE,
                java.util.UUID.randomUUID(),
                CitizenId.newId(),
                null,
                Set.of("Supporting document"),
                AccountId.newId(),
                REQUESTED_AT
        );

        assertThatThrownBy(() -> requested.resolve(AccountId.newId(), REQUESTED_AT.plusSeconds(60)))
                .isInstanceOf(AdditionalDocumentRequestConflictException.class)
                .hasMessageContaining("ADDITIONAL_DOCUMENTS_NOT_SUBMITTED");
    }
}
