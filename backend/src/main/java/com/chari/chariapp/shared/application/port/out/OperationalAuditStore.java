package com.chari.chariapp.shared.application.port.out;

import com.chari.chariapp.shared.application.OperationalAuditEventView;

import java.time.Instant;
import java.util.List;

/** Records immutable operational events without placing personal data in the audit payload. */
public interface OperationalAuditStore {
    void record(String actorAccountId, String action, String targetType, String targetId, String metadata, Instant occurredAt);

    default List<OperationalAuditEventView> findAll() {
        return List.of();
    }
}
