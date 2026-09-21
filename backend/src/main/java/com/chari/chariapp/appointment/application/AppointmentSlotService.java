package com.chari.chariapp.appointment.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.appointment.application.port.out.AppointmentSlotStore;
import com.chari.chariapp.appointment.domain.AppointmentServiceType;
import com.chari.chariapp.appointment.domain.AppointmentSlot;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.operations.application.OperationsPage;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AppointmentSlotService {
    private final PassportRequestActorAccess access;
    private final AppointmentSlotStore slots;
    private final OperationalAuditStore audit;
    private final Clock clock;

    public AppointmentSlotService(PassportRequestActorAccess access, AppointmentSlotStore slots,
                                  OperationalAuditStore audit, Clock clock) {
        this.access = access;
        this.slots = slots;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public AppointmentSlot create(AccountId actor, AppointmentServiceType type, String office,
                                  Instant startsAt, Instant endsAt, int capacity) {
        access.requireActiveOperator(actor);
        requireAppointmentDepartment(type);
        Instant now = Instant.now(clock);
        if (!startsAt.isAfter(now)) throw new IllegalArgumentException("Appointment slot must start in the future");
        AppointmentSlot slot = slots.save(AppointmentSlot.create(type, office, startsAt, endsAt, capacity, actor, now));
        audit.record(actor.value().toString(), "APPOINTMENT_SLOT_CREATED", "APPOINTMENT_SLOT",
                slot.id().toString(), "department=" + type, now);
        return slot;
    }

    public List<AppointmentSlot> available(AccountId actor, AppointmentServiceType type) {
        access.requireActiveCitizen(actor);
        requireAppointmentDepartment(type);
        Instant now = Instant.now(clock);
        return java.util.Arrays.stream(AppointmentServiceType.values())
                .filter(storedType -> storedType.belongsToDepartment(type))
                .flatMap(storedType -> slots.findAvailable(storedType, now).stream())
                .sorted(Comparator.comparing(AppointmentSlot::startsAt))
                .map(AppointmentSlotService::forDepartmentDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public OperationsPage<AppointmentSlot> list(AccountId actor, AppointmentServiceType type, Boolean active,
                                                 Instant startsFrom, Instant startsTo, String office,
                                                 int page, int size) {
        access.requireActiveOperator(actor);
        if (type != null) requireAppointmentDepartment(type);
        validatePage(page, size);
        if (startsFrom != null && startsTo != null && startsFrom.isAfter(startsTo)) {
            throw new IllegalArgumentException("startsFrom must not be after startsTo");
        }
        String normalizedOffice = office == null || office.isBlank() ? null : office.trim().toLowerCase(Locale.ROOT);
        List<AppointmentSlot> filtered = slots.findAll().stream()
                .filter(slot -> type == null || slot.serviceType().belongsToDepartment(type))
                .filter(slot -> active == null || slot.active() == active)
                .filter(slot -> startsFrom == null || !slot.startsAt().isBefore(startsFrom))
                .filter(slot -> startsTo == null || !slot.startsAt().isAfter(startsTo))
                .filter(slot -> normalizedOffice == null || slot.officeName().toLowerCase(Locale.ROOT).contains(normalizedOffice))
                .sorted(Comparator.comparing(AppointmentSlot::startsAt))
                .map(AppointmentSlotService::forDepartmentDisplay)
                .toList();
        return page(filtered, page, size);
    }

    @Transactional
    public AppointmentSlot update(AccountId actor, UUID slotId, AppointmentServiceType type, String office,
                                  Instant startsAt, Instant endsAt, int capacity) {
        access.requireActiveOperator(actor);
        requireAppointmentDepartment(type);
        AppointmentSlot current = require(slotId);
        Instant now = Instant.now(clock);
        try {
            AppointmentSlot updated = slots.save(current.update(type, office, startsAt, endsAt, capacity, now));
            audit.record(actor.value().toString(), "APPOINTMENT_SLOT_UPDATED", "APPOINTMENT_SLOT",
                    slotId.toString(), null, now);
            return updated;
        } catch (IllegalStateException exception) {
            throw new AppointmentConflictException(exception.getMessage());
        }
    }

    @Transactional
    public AppointmentSlot deactivate(AccountId actor, UUID slotId) {
        access.requireActiveOperator(actor);
        AppointmentSlot current = require(slotId);
        Instant now = Instant.now(clock);
        try {
            AppointmentSlot updated = slots.save(current.deactivate());
            audit.record(actor.value().toString(), "APPOINTMENT_SLOT_DEACTIVATED", "APPOINTMENT_SLOT",
                    slotId.toString(), null, now);
            return updated;
        } catch (IllegalStateException exception) {
            throw new AppointmentConflictException(exception.getMessage());
        }
    }

    private AppointmentSlot require(UUID id) {
        return slots.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Appointment slot not found"));
    }

    private static void requireAppointmentDepartment(AppointmentServiceType type) {
        if (type == null || !type.isAppointmentDepartment()) {
            throw new IllegalArgumentException("Appointment department must be PASSPORT or CIVIL_STATUS");
        }
    }

    private static AppointmentSlot forDepartmentDisplay(AppointmentSlot slot) {
        AppointmentServiceType department = slot.serviceType().appointmentDepartment();
        if (slot.serviceType() == department) return slot;
        return new AppointmentSlot(slot.id(), department, slot.officeName(), slot.startsAt(), slot.endsAt(),
                slot.capacity(), slot.reservedCount(), slot.active(), slot.createdBy(), slot.createdAt());
    }

    private static <T> OperationsPage<T> page(List<T> content, int page, int size) {
        int from = (int) Math.min((long) page * size, content.size());
        int to = Math.min(from + size, content.size());
        int totalPages = content.isEmpty() ? 0 : (content.size() + size - 1) / size;
        return new OperationsPage<>(content.subList(from, to), page, size, content.size(), totalPages);
    }

    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be >= 0 and size must be 1-100");
        }
    }
}
