package com.chari.chariapp.settings.domain;

import java.time.Instant;

public record SystemSettings(
        boolean requestSubmissionsEnabled,
        boolean notificationsEnabled,
        String maintenanceMessage,
        String updatedBy,
        Instant updatedAt
) {
}
