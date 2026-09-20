package com.chari.chariapp.settings.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.exception.FeatureDisabledException;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.settings.domain.SystemSettings;
import com.chari.chariapp.settings.infrastructure.persistence.SystemSettingsPersistence;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemSettingsServiceTests {
    private static final Instant NOW = Instant.parse("2026-09-20T08:00:00Z");

    @Test
    void rejectsCitizenRequestSubmissionWhenDisabled() {
        SystemSettingsPersistence persistence = mock(SystemSettingsPersistence.class);
        when(persistence.get()).thenReturn(new SystemSettings(false, true, "Maintenance", null, NOW));
        SystemSettingsService service = service(persistence, mock(OperationalAuditStore.class));

        assertThatThrownBy(service::requireRequestSubmissionsEnabled)
                .isInstanceOf(FeatureDisabledException.class)
                .extracting("code")
                .isEqualTo("REQUEST_SUBMISSIONS_DISABLED");
    }

    @Test
    void updatesAndAuditsSettingsWithoutWritingMessageContentToAudit() {
        PassportRequestActorAccess access = mock(PassportRequestActorAccess.class);
        SystemSettingsPersistence persistence = mock(SystemSettingsPersistence.class);
        OperationalAuditStore audit = mock(OperationalAuditStore.class);
        AccountId actor = AccountId.newId();
        when(persistence.get()).thenReturn(new SystemSettings(true, true, null, null, NOW));
        when(persistence.update(false, false, "Planned maintenance", actor.value().toString(), NOW))
                .thenReturn(new SystemSettings(false, false, "Planned maintenance", actor.value().toString(), NOW));
        SystemSettingsService service = new SystemSettingsService(access, persistence, audit,
                Clock.fixed(NOW, ZoneOffset.UTC));

        service.update(actor, false, false, "Planned maintenance");

        verify(access).requireActiveOperator(actor);
        verify(audit).record(actor.value().toString(), "SYSTEM_SETTINGS_UPDATED", "SYSTEM_SETTINGS", "1",
                "requests=true->false,notifications=true->false,maintenanceMessageChanged=true", NOW);
    }

    private static SystemSettingsService service(SystemSettingsPersistence persistence, OperationalAuditStore audit) {
        return new SystemSettingsService(mock(PassportRequestActorAccess.class), persistence, audit,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }
}
