package com.chari.chariapp.request.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ServiceRequestSubmissionDetails(
        RequestBeneficiaryType beneficiaryType,
        UUID dependentBirthCertificateId,
        String paymentReference,
        String lossReportNumber
) {
    public ServiceRequestSubmissionDetails {
        Objects.requireNonNull(beneficiaryType, "Beneficiary type is required");
        paymentReference = required(paymentReference, "Payment reference", 128);
        lossReportNumber = optional(lossReportNumber, "Loss report number", 128);
        if (beneficiaryType == RequestBeneficiaryType.SELF && dependentBirthCertificateId != null) {
            throw new IllegalArgumentException("A self request cannot reference a dependent birth certificate");
        }
        if (beneficiaryType == RequestBeneficiaryType.DEPENDENT_CHILD && dependentBirthCertificateId == null) {
            throw new IllegalArgumentException("A dependent child request requires a birth certificate");
        }
    }

    private static String required(String value, String label, int maximum) {
        String normalized = optional(value, label, maximum);
        if (normalized == null) throw new IllegalArgumentException(label + " is required");
        return normalized;
    }

    private static String optional(String value, String label, int maximum) {
        String normalized = value == null || value.isBlank() ? null : value.trim();
        if (normalized != null && normalized.length() > maximum) {
            throw new IllegalArgumentException(label + " must not exceed " + maximum + " characters");
        }
        return normalized;
    }
}
