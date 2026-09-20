package com.chari.chariapp.settings.infrastructure.persistence;

import com.chari.chariapp.settings.domain.SystemSettings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "system_settings")
class SystemSettingsJpaEntity {
    @Id private Integer id;
    @Column(name = "request_submissions_enabled", nullable = false) private boolean requestSubmissionsEnabled;
    @Column(name = "notifications_enabled", nullable = false) private boolean notificationsEnabled;
    @Column(name = "maintenance_message", length = 500) private String maintenanceMessage;
    @Column(name = "updated_by", length = 36, columnDefinition = "CHAR(36)") private String updatedBy;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version private Long version;

    protected SystemSettingsJpaEntity() {
    }

    SystemSettings toDomain() {
        return new SystemSettings(requestSubmissionsEnabled, notificationsEnabled, maintenanceMessage, updatedBy, updatedAt);
    }

    void apply(boolean requestsEnabled, boolean notificationDeliveryEnabled, String message,
               String actorAccountId, Instant now) {
        id = 1;
        requestSubmissionsEnabled = requestsEnabled;
        notificationsEnabled = notificationDeliveryEnabled;
        maintenanceMessage = normalize(message);
        updatedBy = actorAccountId;
        updatedAt = now;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > 500) throw new IllegalArgumentException("Maintenance message must not exceed 500 characters");
        return normalized;
    }
}
