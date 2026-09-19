package com.chari.chariapp.shared.application;

import java.time.Instant;

public record OperationalAuditEventView(
        String id,
        String actorAccountId,
        String action,
        String targetType,
        String targetId,
        String result,
        String metadata,
        Instant occurredAt
) {
}
