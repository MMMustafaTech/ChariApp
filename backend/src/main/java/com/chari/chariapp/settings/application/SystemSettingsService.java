package com.chari.chariapp.settings.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.exception.FeatureDisabledException;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.settings.domain.SystemSettings;
import com.chari.chariapp.settings.infrastructure.persistence.SystemSettingsPersistence;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class SystemSettingsService {
    private final PassportRequestActorAccess access;
    private final SystemSettingsPersistence persistence;
    private final OperationalAuditStore audit;
    private final Clock clock;

    public SystemSettingsService(PassportRequestActorAccess access, SystemSettingsPersistence persistence,
                                 OperationalAuditStore audit, Clock clock) {
        this.access = access;
        this.persistence = persistence;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SystemSettings get(AccountId actorId) {
        access.requireActiveOperator(actorId);
        return persistence.get();
    }

    @Transactional
    public SystemSettings update(AccountId actorId, boolean requestSubmissionsEnabled,
                                 boolean notificationsEnabled, String maintenanceMessage) {
        access.requireActiveOperator(actorId);
        SystemSettings before = persistence.get();
        Instant now = Instant.now(clock);
        SystemSettings updated = persistence.update(requestSubmissionsEnabled, notificationsEnabled,
                maintenanceMessage, actorId.value().toString(), now);
        audit.record(actorId.value().toString(), "SYSTEM_SETTINGS_UPDATED", "SYSTEM_SETTINGS", "1",
                "requests=" + before.requestSubmissionsEnabled() + "->" + updated.requestSubmissionsEnabled()
                        + ",notifications=" + before.notificationsEnabled() + "->" + updated.notificationsEnabled()
                        + ",maintenanceMessageChanged=" + !java.util.Objects.equals(before.maintenanceMessage(), updated.maintenanceMessage()),
                now);
        return updated;
    }

    @Transactional(readOnly = true)
    public void requireRequestSubmissionsEnabled() {
        if (!persistence.get().requestSubmissionsEnabled()) {
            throw new FeatureDisabledException("REQUEST_SUBMISSIONS_DISABLED", "Request submissions are disabled");
        }
    }

    @Transactional(readOnly = true)
    public void requireNotificationsEnabled() {
        if (!notificationsEnabled()) {
            throw new FeatureDisabledException("NOTIFICATIONS_DISABLED", "Notifications are disabled");
        }
    }

    @Transactional(readOnly = true)
    public boolean notificationsEnabled() {
        return persistence.get().notificationsEnabled();
    }
}
