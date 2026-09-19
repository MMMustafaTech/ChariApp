package com.chari.chariapp.document.infrastructure.web;

import com.chari.chariapp.document.domain.MyNationalIdentity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MyDocumentsResponseMappingTests {

    @Test
    void mapsNationalIdentityToFlutterFieldNamesAndCompatibilityAlias() {
        MyNationalIdentity identity = new MyNationalIdentity(
                "CID002", "Ahmed", "Saleh", "Male", "Moundou",
                LocalDate.of(1992, 7, 21), "AA6394722", "Moundou",
                LocalDate.of(2014, 2, 11), LocalDate.of(2024, 2, 11),
                "Teacher", "Saleh Ibrahim", "Aisha Saleh", "Moundou", "B+"
        );

        MyDocumentsController.NationalIdentityResponse response =
                MyDocumentsController.NationalIdentityResponse.from(identity);

        assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(1992, 7, 21));
        assertThat(response.dateofBirth()).isEqualTo(response.dateOfBirth());
        assertThat(response.issueDetails()).isEqualTo("Moundou/2014-02-11");
        assertThat(response.dateOfExpiry()).isEqualTo(LocalDate.of(2024, 2, 11));
    }
}
