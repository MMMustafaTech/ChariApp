package com.chari.chariapp.appointment.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.appointment.application.port.out.AppointmentSlotStore;
import com.chari.chariapp.appointment.application.port.out.AppointmentStore;
import com.chari.chariapp.appointment.domain.*;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.notification.domain.NotificationType;
import com.chari.chariapp.operations.application.OperationsPage;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AppointmentService {
    private final PassportRequestActorAccess access;
    private final AppointmentSlotStore slots;
    private final AppointmentStore appointments;
    private final CitizenStore citizens;
    private final PersonalDataProtector protector;
    private final OperationalAuditStore audit;
    private final NotificationService notifications;
    private final Clock clock;

    public AppointmentService(PassportRequestActorAccess access, AppointmentSlotStore slots,
                              AppointmentStore appointments, CitizenStore citizens,
                              PersonalDataProtector protector, OperationalAuditStore audit,
                              NotificationService notifications, Clock clock) {
        this.access = access;
        this.slots = slots;
        this.appointments = appointments;
        this.citizens = citizens;
        this.protector = protector;
        this.audit = audit;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Transactional
    public Appointment book(AccountId actor, UUID slotId) {
        CitizenId citizen = access.requireActiveCitizen(actor);
        AppointmentSlot slot = slots.findByIdForUpdate(slotId)
                .orElseThrow(() -> new NotFoundException("Appointment slot not found"));
        if (appointments.hasActiveAppointment(citizen, slot.serviceType())) {
            throw new AppointmentConflictException("An active appointment already exists for this service");
        }
        Instant now = Instant.now(clock);
        try {
            AppointmentSlot reservedSlot = slot.reserve(now);
            Appointment appointment = Appointment.book(citizen, reservedSlot, now);
            appointments.save(appointment);
            slots.save(reservedSlot);
            audit.record(actor.value().toString(), "APPOINTMENT_BOOKED", "APPOINTMENT",
                    appointment.id().toString(), "slotId=" + slotId, now);
            notifications.publish(citizen, NotificationType.APPOINTMENT_BOOKED,
                    "Appointment booked", "Your appointment has been booked successfully.");
            return appointment;
        } catch (DataIntegrityViolationException exception) {
            throw new AppointmentConflictException("An active appointment already exists for this service");
        } catch (IllegalStateException exception) {
            throw new AppointmentConflictException(exception.getMessage());
        }
    }

    @Transactional
    public Appointment cancelMine(AccountId actor, UUID appointmentId) {
        CitizenId citizen = access.requireActiveCitizen(actor);
        return cancel(actor, owned(citizen, appointmentId), "APPOINTMENT_CANCELLED");
    }

    @Transactional
    public Appointment cancelByOperator(AccountId actor, UUID appointmentId) {
        access.requireActiveOperator(actor);
        Appointment current = appointments.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        return cancel(actor, current, "APPOINTMENT_CANCELLED_BY_OPERATOR");
    }

    @Transactional
    public Appointment complete(AccountId actor, UUID appointmentId) {
        access.requireActiveOperator(actor);
        Appointment current = appointments.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        Instant now = Instant.now(clock);
        try {
            Appointment updated = current.complete(actor, now);
            appointments.save(updated);
            audit.record(actor.value().toString(), "APPOINTMENT_COMPLETED", "APPOINTMENT",
                    appointmentId.toString(), null, now);
            notifications.publish(updated.citizenId(), NotificationType.APPOINTMENT_COMPLETED,
                    "Appointment completed", "Your appointment has been marked as completed.");
            return updated;
        } catch (IllegalStateException exception) {
            throw new AppointmentConflictException(exception.getMessage());
        }
    }

    public List<Appointment> mine(AccountId actor) {
        return appointments.findByCitizenId(access.requireActiveCitizen(actor));
    }

    public List<Appointment> byStatus(AccountId actor, AppointmentStatus status) {
        access.requireActiveOperator(actor);
        return appointments.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public OperationsPage<AppointmentOperationsView> search(AccountId actor, AppointmentStatus status,
                                                             AppointmentServiceType serviceType,
                                                             Instant startsFrom, Instant startsTo,
                                                             String query, int page, int size) {
        access.requireActiveOperator(actor);
        validatePage(page, size);
        if (startsFrom != null && startsTo != null && startsFrom.isAfter(startsTo)) {
            throw new IllegalArgumentException("startsFrom must not be after startsTo");
        }
        String normalized = query == null || query.isBlank() ? null : query.trim().toLowerCase(Locale.ROOT);
        List<AppointmentOperationsView> filtered = appointments.findAll().stream()
                .filter(value -> status == null || value.status() == status)
                .filter(value -> serviceType == null || value.serviceType() == serviceType)
                .filter(value -> startsFrom == null || !value.startsAt().isBefore(startsFrom))
                .filter(value -> startsTo == null || !value.startsAt().isAfter(startsTo))
                .map(this::view)
                .filter(value -> matches(value, normalized))
                .sorted(Comparator.comparing(AppointmentOperationsView::startsAt))
                .toList();
        int from = (int) Math.min((long) page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        int totalPages = filtered.isEmpty() ? 0 : (filtered.size() + size - 1) / size;
        return new OperationsPage<>(filtered.subList(from, to), page, size, filtered.size(), totalPages);
    }

    private Appointment cancel(AccountId actor, Appointment current, String auditAction) {
        Instant now = Instant.now(clock);
        try {
            Appointment updated = current.cancel(now);
            AppointmentSlot slot = slots.findByIdForUpdate(current.slotId())
                    .orElseThrow(() -> new NotFoundException("Appointment slot not found"));
            slots.save(slot.release());
            appointments.save(updated);
            audit.record(actor.value().toString(), auditAction, "APPOINTMENT",
                    current.id().toString(), null, now);
            notifications.publish(updated.citizenId(), NotificationType.APPOINTMENT_CANCELLED,
                    "Appointment cancelled", "Your appointment has been cancelled.");
            return updated;
        } catch (IllegalStateException exception) {
            throw new AppointmentConflictException(exception.getMessage());
        }
    }

    private Appointment owned(CitizenId citizen, UUID id) {
        Appointment appointment = appointments.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        if (!appointment.belongsTo(citizen)) throw new NotFoundException("Appointment not found");
        return appointment;
    }

    private AppointmentOperationsView view(Appointment value) {
        Citizen citizen = citizens.findById(value.citizenId())
                .orElseThrow(() -> new NotFoundException("Citizen not found"));
        return new AppointmentOperationsView(value.id(), value.citizenId().value(),
                protector.decrypt(citizen.nationalId().ciphertext()), value.slotId(), value.serviceType(),
                value.officeName(), value.startsAt(), value.endsAt(), value.status(), value.bookedAt(),
                value.cancelledAt(), value.completedAt(),
                value.completedBy() == null ? null : value.completedBy().value());
    }

    private static boolean matches(AppointmentOperationsView value, String query) {
        if (query == null) return true;
        return value.id().toString().toLowerCase(Locale.ROOT).contains(query)
                || value.citizenId().toString().toLowerCase(Locale.ROOT).contains(query)
                || value.citizenNationalId().toLowerCase(Locale.ROOT).contains(query)
                || value.officeName().toLowerCase(Locale.ROOT).contains(query);
    }

    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be >= 0 and size must be 1-100");
        }
    }
}
