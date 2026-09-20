package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.notification.application.port.out.NotificationStore;
import com.chari.chariapp.notification.domain.NotificationType;
import com.chari.chariapp.notification.domain.UserNotification;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OperationsNotificationService {
    private final PassportRequestActorAccess access;
    private final NotificationStore store;
    private final NotificationService notifications;
    private final CitizenStore citizens;
    private final OperationalAuditStore audit;
    private final Clock clock;

    public OperationsNotificationService(PassportRequestActorAccess access, NotificationStore store,
                                         NotificationService notifications, CitizenStore citizens,
                                         OperationalAuditStore audit, Clock clock) {
        this.access = access;
        this.store = store;
        this.notifications = notifications;
        this.citizens = citizens;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public OperationsPage<UserNotification> list(AccountId actorId, UUID citizenId, NotificationType type,
                                                  Boolean read, int page, int size) {
        access.requireActiveOperator(actorId);
        validatePage(page, size);
        List<UserNotification> filtered = store.findAll().stream()
                .filter(value -> citizenId == null || value.citizenId().value().equals(citizenId))
                .filter(value -> type == null || value.type() == type)
                .filter(value -> read == null || read == (value.readAt() != null))
                .toList();
        int from = (int) Math.min((long) page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        int totalPages = filtered.isEmpty() ? 0 : (filtered.size() + size - 1) / size;
        return new OperationsPage<>(filtered.subList(from, to), page, size, filtered.size(), totalPages);
    }

    @Transactional
    public UserNotification send(AccountId actorId, UUID citizenId, NotificationType type,
                                 String title, String message) {
        access.requireActiveOperator(actorId);
        notifications.requireEnabled();
        CitizenId recipient = new CitizenId(citizenId);
        if (citizens.findById(recipient).isEmpty()) throw new NotFoundException("Citizen not found");
        UserNotification notification = notifications.publish(recipient, type, title, message);
        Instant now = Instant.now(clock);
        audit.record(actorId.value().toString(), "NOTIFICATION_SENT", "USER_NOTIFICATION",
                notification.id().toString(), "citizenId=" + citizenId + ",type=" + type.name(), now);
        return notification;
    }

    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be >= 0 and size must be 1-100");
        }
    }
}
