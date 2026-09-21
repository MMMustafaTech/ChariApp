package com.chari.chariapp.appointment.domain;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Appointment(UUID id, CitizenId citizenId, UUID slotId,
                          @JsonProperty("department") AppointmentServiceType serviceType, String officeName,
                          Instant startsAt, Instant endsAt, AppointmentStatus status, Instant bookedAt, Instant cancelledAt,
                          Instant completedAt, AccountId completedBy) {
    public Appointment {
        Objects.requireNonNull(id); Objects.requireNonNull(citizenId); Objects.requireNonNull(slotId); Objects.requireNonNull(serviceType); Objects.requireNonNull(officeName); Objects.requireNonNull(startsAt); Objects.requireNonNull(endsAt); Objects.requireNonNull(status); Objects.requireNonNull(bookedAt);
        if (!endsAt.isAfter(startsAt)) throw new IllegalArgumentException("Appointment end time must be after its start time");
        if (status == AppointmentStatus.BOOKED && (cancelledAt != null || completedAt != null || completedBy != null)) throw new IllegalArgumentException("Booked appointment cannot have a final state");
        if (status == AppointmentStatus.CANCELLED && cancelledAt == null) throw new IllegalArgumentException("Cancelled appointment requires a cancellation time");
        if (status == AppointmentStatus.COMPLETED && (completedAt == null || completedBy == null)) throw new IllegalArgumentException("Completed appointment requires completion data");
    }
    public static Appointment book(CitizenId citizenId, AppointmentSlot slot, Instant now) { return new Appointment(UUID.randomUUID(), citizenId, slot.id(), slot.serviceType().appointmentDepartment(), slot.officeName(), slot.startsAt(), slot.endsAt(), AppointmentStatus.BOOKED, now, null, null, null); }
    public Appointment cancel(Instant now) { if (status != AppointmentStatus.BOOKED) throw new IllegalStateException("Only a booked appointment can be cancelled"); if (!startsAt.isAfter(now)) throw new IllegalStateException("An appointment can no longer be cancelled after it starts"); return new Appointment(id, citizenId, slotId, serviceType, officeName, startsAt, endsAt, AppointmentStatus.CANCELLED, bookedAt, now, null, null); }
    public Appointment complete(AccountId operator, Instant now) { if (status != AppointmentStatus.BOOKED) throw new IllegalStateException("Only a booked appointment can be completed"); return new Appointment(id, citizenId, slotId, serviceType, officeName, startsAt, endsAt, AppointmentStatus.COMPLETED, bookedAt, null, now, operator); }
    public boolean belongsTo(CitizenId candidate) { return citizenId.equals(candidate); }
    public Appointment forDepartmentDisplay() { return serviceType.isAppointmentDepartment() ? this : new Appointment(id, citizenId, slotId, serviceType.appointmentDepartment(), officeName, startsAt, endsAt, status, bookedAt, cancelledAt, completedAt, completedBy); }
}
