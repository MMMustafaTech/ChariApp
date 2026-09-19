package com.chari.chariapp.account.domain;

import com.chari.chariapp.citizen.domain.PhoneReference;

import java.time.Instant;
import java.util.Objects;

public record StaffProfile(
        AccountId accountId,
        String employeeNumber,
        String firstName,
        String lastName,
        PhoneReference phone,
        String jobTitle,
        Instant createdAt,
        Instant updatedAt
) {
    public StaffProfile {
        Objects.requireNonNull(accountId, "Account ID is required");
        employeeNumber = required(employeeNumber, 32, "Employee number");
        firstName = optional(firstName, 100, "First name");
        lastName = optional(lastName, 100, "Last name");
        jobTitle = optional(jobTitle, 120, "Job title");
        Objects.requireNonNull(createdAt, "Creation time is required");
        Objects.requireNonNull(updatedAt, "Update time is required");
    }

    public StaffProfile update(String firstName, String lastName, PhoneReference phone, String jobTitle, Instant now) {
        return new StaffProfile(accountId, employeeNumber, firstName, lastName, phone, jobTitle, createdAt, now);
    }

    private static String required(String value, int max, String label) {
        String normalized = optional(value, max, label);
        if (normalized == null) throw new IllegalArgumentException(label + " is required");
        return normalized;
    }

    private static String optional(String value, int max, String label) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > max) throw new IllegalArgumentException(label + " must not exceed " + max + " characters");
        return normalized;
    }
}
