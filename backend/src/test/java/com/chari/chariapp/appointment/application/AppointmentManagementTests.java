package com.chari.chariapp.appointment.application;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.domain.*;
import com.chari.chariapp.appointment.application.port.out.AppointmentSlotStore;
import com.chari.chariapp.appointment.application.port.out.AppointmentStore;
import com.chari.chariapp.appointment.domain.*;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.notification.domain.NotificationType;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AppointmentManagementTests {
    private static final Instant NOW = Instant.parse("2026-09-19T13:00:00Z");

    @Test
    void slotWithReservationsCannotChangeTimeOrBeDeactivated() {
        AccountId creator = AccountId.newId();
        AppointmentSlot reserved = new AppointmentSlot(
                java.util.UUID.randomUUID(), AppointmentServiceType.PASSPORT, "Main office",
                NOW.plusSeconds(3600), NOW.plusSeconds(7200), 10, 1, true, creator, NOW.minusSeconds(60)
        );

        assertThatThrownBy(() -> reserved.update(AppointmentServiceType.PASSPORT, "Main office",
                NOW.plusSeconds(4000), NOW.plusSeconds(7600), 10, NOW))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reservations");
        assertThatThrownBy(reserved::deactivate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cancel booked appointments");
    }

    @Test
    void operatorCancellationReleasesCapacityAndNotifiesCitizen() {
        AccountId employeeId = AccountId.newId();
        CitizenId citizenId = CitizenId.newId();
        AppointmentSlot slot = new AppointmentSlot(
                java.util.UUID.randomUUID(), AppointmentServiceType.NATIONAL_IDENTITY, "Office",
                NOW.plusSeconds(3600), NOW.plusSeconds(7200), 5, 1, true, employeeId, NOW.minusSeconds(60)
        );
        Appointment appointment = Appointment.book(citizenId, slot, NOW.minusSeconds(30));

        AccountStore accounts = mock(AccountStore.class);
        AppointmentSlotStore slots = mock(AppointmentSlotStore.class);
        AppointmentStore appointments = mock(AppointmentStore.class);
        OperationalAuditStore audit = mock(OperationalAuditStore.class);
        NotificationService notifications = mock(NotificationService.class);
        when(accounts.findById(employeeId)).thenReturn(Optional.of(employee(employeeId)));
        when(appointments.findByIdForUpdate(appointment.id())).thenReturn(Optional.of(appointment));
        when(slots.findByIdForUpdate(slot.id())).thenReturn(Optional.of(slot));
        when(slots.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentService service = new AppointmentService(
                new PassportRequestActorAccess(accounts), slots, appointments, mock(CitizenStore.class),
                mock(PersonalDataProtector.class), audit, notifications,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        Appointment cancelled = service.cancelByOperator(employeeId, appointment.id());

        assertThat(cancelled.status()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(slots).save(argThat(updated -> updated.reservedCount() == 0));
        verify(notifications).publish(citizenId, NotificationType.APPOINTMENT_CANCELLED,
                "Appointment cancelled", "Your appointment has been cancelled.");
        verify(audit).record(employeeId.value().toString(), "APPOINTMENT_CANCELLED_BY_OPERATOR",
                "APPOINTMENT", appointment.id().toString(), null, NOW);
    }

    private static Account employee(AccountId id) {
        return new Account(id, null, new EmailReference("a".repeat(64), "ciphertext"), "hash",
                AccountStatus.ACTIVE, Set.of(AccountRole.EMPLOYEE),
                Set.of(StaffPermission.APPOINTMENT_VIEW, StaffPermission.APPOINTMENT_MANAGE), NOW);
    }
}
