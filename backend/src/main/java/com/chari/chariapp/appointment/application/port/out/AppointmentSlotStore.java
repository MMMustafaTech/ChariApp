package com.chari.chariapp.appointment.application.port.out;

import com.chari.chariapp.appointment.domain.*;
import java.time.Instant;
import java.util.*;
public interface AppointmentSlotStore {
    Optional<AppointmentSlot> findByIdForUpdate(UUID id);
    AppointmentSlot save(AppointmentSlot slot);
    List<AppointmentSlot> findAvailable(AppointmentServiceType type, Instant now);
    default List<AppointmentSlot> findAll() { return List.of(); }
}
