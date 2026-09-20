package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.notification.application.port.out.NotificationStore;
import com.chari.chariapp.notification.domain.NotificationType;
import com.chari.chariapp.notification.domain.UserNotification;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationsNotificationServiceTests {
    private static final Instant NOW = Instant.parse("2026-09-20T08:00:00Z");

    @Test
    void sendsManualNotificationAndRecordsAuditEvent() {
        PassportRequestActorAccess access = mock(PassportRequestActorAccess.class);
        NotificationStore store = mock(NotificationStore.class);
        NotificationService notifications = mock(NotificationService.class);
        CitizenStore citizens = mock(CitizenStore.class);
        OperationalAuditStore audit = mock(OperationalAuditStore.class);
        AccountId actor = AccountId.newId();
        CitizenId citizenId = CitizenId.newId();
        UUID notificationId = UUID.randomUUID();
        Citizen citizen = new Citizen(citizenId, new NationalIdReference("a".repeat(64), "cipher"),
                null, null, NOW);
        UserNotification sent = new UserNotification(notificationId, citizenId, NotificationType.GENERAL,
                "Update", "Your request changed", null, NOW);
        when(citizens.findById(citizenId)).thenReturn(Optional.of(citizen));
        when(notifications.publish(citizenId, NotificationType.GENERAL, "Update", "Your request changed"))
                .thenReturn(sent);
        OperationsNotificationService service = new OperationsNotificationService(access, store, notifications,
                citizens, audit, Clock.fixed(NOW, ZoneOffset.UTC));

        UserNotification result = service.send(actor, citizenId.value(), NotificationType.GENERAL,
                "Update", "Your request changed");

        assertThat(result).isEqualTo(sent);
        verify(notifications).requireEnabled();
        verify(audit).record(actor.value().toString(), "NOTIFICATION_SENT", "USER_NOTIFICATION",
                notificationId.toString(), "citizenId=" + citizenId.value() + ",type=GENERAL", NOW);
    }
}
