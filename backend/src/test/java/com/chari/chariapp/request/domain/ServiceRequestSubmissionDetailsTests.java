package com.chari.chariapp.request.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceRequestSubmissionDetailsTests {

    @Test
    void acceptsSelfSubmissionDetails() {
        ServiceRequestSubmissionDetails details = new ServiceRequestSubmissionDetails(
                RequestBeneficiaryType.SELF, null, "PAY-001", null);

        assertThat(details.paymentReference()).isEqualTo("PAY-001");
    }

    @Test
    void dependentChildRequiresCertificate() {
        assertThatThrownBy(() -> new ServiceRequestSubmissionDetails(
                RequestBeneficiaryType.DEPENDENT_CHILD, null, "PAY-001", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("birth certificate");

        ServiceRequestSubmissionDetails details = new ServiceRequestSubmissionDetails(
                RequestBeneficiaryType.DEPENDENT_CHILD, UUID.randomUUID(), "PAY-001", null);
        assertThat(details.dependentBirthCertificateId()).isNotNull();
    }

    @Test
    void selfRequestRejectsDependentCertificateAndRequiresPaymentReference() {
        assertThatThrownBy(() -> new ServiceRequestSubmissionDetails(
                RequestBeneficiaryType.SELF, UUID.randomUUID(), "PAY-001", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("self request");

        assertThatThrownBy(() -> new ServiceRequestSubmissionDetails(
                RequestBeneficiaryType.SELF, null, " ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment reference");
    }
}
