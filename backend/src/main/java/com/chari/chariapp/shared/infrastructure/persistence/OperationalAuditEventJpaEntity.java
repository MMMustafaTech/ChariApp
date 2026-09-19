package com.chari.chariapp.shared.infrastructure.persistence;

import com.chari.chariapp.shared.application.OperationalAuditEventView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class OperationalAuditEventJpaEntity {
    @Id @Column(length = 36, nullable = false, updatable = false, columnDefinition = "CHAR(36)") private String id;
    @Column(name = "actor_account_id", length = 36, columnDefinition = "CHAR(36)") private String actorAccountId;
    @Column(nullable = false, length = 128) private String action;
    @Column(name = "target_type", nullable = false, length = 64) private String targetType;
    @Column(name = "target_id", length = 36, columnDefinition = "CHAR(36)") private String targetId;
    @Column(nullable = false, length = 32) private String result;
    @Column(columnDefinition = "TEXT") private String metadata;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;

    protected OperationalAuditEventJpaEntity() { }

    static OperationalAuditEventJpaEntity success(
            String actorAccountId, String action, String targetType, String targetId, String metadata, Instant occurredAt
    ) {
        OperationalAuditEventJpaEntity event = new OperationalAuditEventJpaEntity();
        event.id = UUID.randomUUID().toString();
        event.actorAccountId = actorAccountId;
        event.action = action;
        event.targetType = targetType;
        event.targetId = targetId;
        event.result = "SUCCESS";
        event.metadata = metadata;
        event.occurredAt = occurredAt;
        return event;
    }

    OperationalAuditEventView toView() {
        return new OperationalAuditEventView(id, actorAccountId, action, targetType, targetId,
                result, metadata, occurredAt);
    }
}
