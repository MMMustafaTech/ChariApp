package com.chari.chariapp.appointment.application;

import com.chari.chariapp.appointment.domain.AppointmentServiceType;
import com.chari.chariapp.appointment.domain.AppointmentStatus;

import java.time.Instant;
import java.util.UUID;

public record AppointmentOperationsView(
        UUID id,
        UUID citizenId,
        String citizenNationalId,
        UUID slotId,
        AppointmentServiceType department,
        String officeName,
        Instant startsAt,
        Instant endsAt,
        AppointmentStatus status,
        Instant bookedAt,
        Instant cancelledAt,
        Instant completedAt,
        UUID completedBy
) {
}
