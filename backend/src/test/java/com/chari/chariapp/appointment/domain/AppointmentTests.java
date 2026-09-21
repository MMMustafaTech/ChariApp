package com.chari.chariapp.appointment.domain;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AppointmentTests {
    private static final Instant NOW = Instant.parse("2026-08-29T00:00:00Z");

    @Test
    void slotDoesNotAcceptMoreBookingsThanItsCapacity() {
        AppointmentSlot slot = AppointmentSlot.create(AppointmentServiceType.PASSPORT, "Central Office", NOW.plusSeconds(3600), NOW.plusSeconds(5400), 1, AccountId.newId(), NOW);
        AppointmentSlot reserved = slot.reserve(NOW);
        assertThat(reserved.reservedCount()).isEqualTo(1);
        assertThatThrownBy(() -> reserved.reserve(NOW)).isInstanceOf(IllegalStateException.class).hasMessage("Appointment slot is full");
    }

    @Test
    void citizenCannotCancelAppointmentAfterItStarts() {
        AppointmentSlot slot = AppointmentSlot.create(AppointmentServiceType.CIVIL_STATUS, "Central Office", NOW.plusSeconds(10), NOW.plusSeconds(1200), 2, AccountId.newId(), NOW);
        Appointment appointment = Appointment.book(CitizenId.newId(), slot, NOW);
        assertThatThrownBy(() -> appointment.cancel(NOW.plusSeconds(10))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void completingAppointmentRecordsTheResponsibleOperator() {
        AppointmentSlot slot = AppointmentSlot.create(AppointmentServiceType.CIVIL_STATUS, "Central Office", NOW.plusSeconds(3600), NOW.plusSeconds(5400), 2, AccountId.newId(), NOW);
        AccountId operator = AccountId.newId();
        Appointment completed = Appointment.book(CitizenId.newId(), slot, NOW).complete(operator, NOW.plusSeconds(3600));
        assertThat(completed.status()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(completed.completedBy()).isEqualTo(operator);
    }

    @Test
    void legacyCivilServicesAreGroupedUnderCivilStatusAppointments() {
        assertThat(AppointmentServiceType.NATIONAL_IDENTITY.appointmentDepartment())
                .isEqualTo(AppointmentServiceType.CIVIL_STATUS);
        assertThat(AppointmentServiceType.BIRTH_CERTIFICATE.appointmentDepartment())
                .isEqualTo(AppointmentServiceType.CIVIL_STATUS);
        assertThat(AppointmentServiceType.PASSPORT.appointmentDepartment())
                .isEqualTo(AppointmentServiceType.PASSPORT);
    }
}
