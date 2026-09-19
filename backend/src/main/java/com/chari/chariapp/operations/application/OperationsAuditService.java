package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.OperationalAuditEventView;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class OperationsAuditService {
    private final PassportRequestActorAccess access;
    private final OperationalAuditStore audit;

    public OperationsAuditService(PassportRequestActorAccess access, OperationalAuditStore audit) {
        this.access = access;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public OperationsPage<OperationalAuditEventView> list(AccountId actorId, String actorAccountId,
                                                           String action, String targetType, String targetId,
                                                           Instant occurredFrom, Instant occurredTo,
                                                           int page, int size) {
        access.requireActiveOperator(actorId);
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be >= 0 and size must be 1-100");
        }
        if (occurredFrom != null && occurredTo != null && occurredFrom.isAfter(occurredTo)) {
            throw new IllegalArgumentException("occurredFrom must not be after occurredTo");
        }
        String normalizedAction = normalize(action);
        String normalizedTargetType = normalize(targetType);
        List<OperationalAuditEventView> filtered = audit.findAll().stream()
                .filter(event -> actorAccountId == null || actorAccountId.equalsIgnoreCase(event.actorAccountId()))
                .filter(event -> normalizedAction == null || event.action().toLowerCase(Locale.ROOT).contains(normalizedAction))
                .filter(event -> normalizedTargetType == null || event.targetType().toLowerCase(Locale.ROOT).equals(normalizedTargetType))
                .filter(event -> targetId == null || targetId.equalsIgnoreCase(event.targetId()))
                .filter(event -> occurredFrom == null || !event.occurredAt().isBefore(occurredFrom))
                .filter(event -> occurredTo == null || !event.occurredAt().isAfter(occurredTo))
                .toList();
        int from = (int) Math.min((long) page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        int totalPages = filtered.isEmpty() ? 0 : (filtered.size() + size - 1) / size;
        return new OperationsPage<>(filtered.subList(from, to), page, size, filtered.size(), totalPages);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
