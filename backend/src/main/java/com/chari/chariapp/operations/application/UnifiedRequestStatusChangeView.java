package com.chari.chariapp.operations.application;

import com.chari.chariapp.operations.domain.UnifiedRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record UnifiedRequestStatusChangeView(
        UUID id,
        UUID requestId,
        UnifiedRequestStatus fromStatus,
        UnifiedRequestStatus toStatus,
        String reason,
        UUID changedBy,
        Instant changedAt
) {
}
