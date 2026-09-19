package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.domain.*;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.OperationalAuditEventView;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperationsAuditServiceTests {
    private static final Instant NOW = Instant.parse("2026-09-19T13:00:00Z");

    @Test
    void filtersAndPaginatesAuditEvents() {
        PassportRequestActorAccess access = mock(PassportRequestActorAccess.class);
        OperationalAuditStore audit = mock(OperationalAuditStore.class);
        AccountId operatorId = AccountId.newId();
        when(access.requireActiveOperator(operatorId)).thenReturn(operator(operatorId));
        when(audit.findAll()).thenReturn(List.of(
                event("2", "actor-2", "APPOINTMENT_CANCELLED_BY_OPERATOR", "APPOINTMENT", "target-2", NOW),
                event("1", "actor-1", "PASSPORT_REQUEST_APPROVED", "SERVICE_REQUEST", "target-1", NOW.minusSeconds(60))
        ));
        OperationsAuditService service = new OperationsAuditService(access, audit);

        OperationsPage<OperationalAuditEventView> page = service.list(operatorId, null, "appointment",
                "APPOINTMENT", null, NOW.minusSeconds(1), NOW.plusSeconds(1), 0, 20);

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content()).singleElement().satisfies(event ->
                assertThat(event.action()).isEqualTo("APPOINTMENT_CANCELLED_BY_OPERATOR"));
    }

    private static OperationalAuditEventView event(String id, String actor, String action,
                                                    String targetType, String targetId, Instant at) {
        return new OperationalAuditEventView(id, actor, action, targetType, targetId,
                "SUCCESS", null, at);
    }

    private static Account operator(AccountId id) {
        return new Account(id, null, new EmailReference("a".repeat(64), "cipher"), "hash",
                AccountStatus.ACTIVE, Set.of(AccountRole.ADMIN),
                Set.of(StaffPermission.AUDIT_VIEW), NOW.minusSeconds(3600));
    }
}
