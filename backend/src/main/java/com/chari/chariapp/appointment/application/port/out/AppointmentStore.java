package com.chari.chariapp.appointment.application.port.out;

import com.chari.chariapp.appointment.domain.*;
import com.chari.chariapp.citizen.domain.CitizenId;
import java.util.*;
public interface AppointmentStore {
    boolean hasActiveAppointment(CitizenId citizenId, AppointmentServiceType serviceType);
    Optional<Appointment> findByIdForUpdate(UUID id);
    Appointment save(Appointment appointment);
    List<Appointment> findByCitizenId(CitizenId citizenId);
    List<Appointment> findByStatus(AppointmentStatus status);
    default List<Appointment> findAll() { return List.of(); }
}
