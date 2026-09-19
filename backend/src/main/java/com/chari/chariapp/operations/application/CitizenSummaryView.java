package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.domain.AccountStatus;

import java.time.Instant;
import java.util.UUID;

public record CitizenSummaryView(
        UUID citizenId,
        UUID accountId,
        String nationalId,
        String fullName,
        String email,
        String phoneNumber,
        boolean phoneVerified,
        AccountStatus accountStatus,
        Instant createdAt
) {
}
