package com.chari.chariapp.appointment.infrastructure.persistence;
import com.chari.chariapp.appointment.application.port.out.AppointmentStore; import com.chari.chariapp.appointment.domain.*; import com.chari.chariapp.citizen.domain.CitizenId; import java.util.*; import org.springframework.stereotype.Repository;
@Repository public class JpaAppointmentStore implements AppointmentStore {
 private final SpringDataAppointmentRepository repository; public JpaAppointmentStore(SpringDataAppointmentRepository repository){this.repository=repository;}
 public boolean hasActiveAppointment(CitizenId citizen,AppointmentServiceType type){return repository.hasActive(citizen.value().toString(),type.name());}
 public Optional<Appointment> findByIdForUpdate(UUID id){return repository.findForUpdate(id.toString()).map(AppointmentJpaEntity::toDomain);}
 public Appointment save(Appointment appointment){return repository.findById(appointment.id().toString()).map(e->{e.apply(appointment);return repository.save(e);}).orElseGet(()->repository.save(AppointmentJpaEntity.from(appointment))).toDomain();}
 public List<Appointment> findByCitizenId(CitizenId citizen){return repository.findByCitizenIdOrderByBookedAtDesc(citizen.value().toString()).stream().map(AppointmentJpaEntity::toDomain).toList();}
 public List<Appointment> findByStatus(AppointmentStatus status){return repository.findByStatusOrderByStartsAtAsc(status).stream().map(AppointmentJpaEntity::toDomain).toList();}
 public List<Appointment> findAll(){return repository.findAll().stream().map(AppointmentJpaEntity::toDomain).toList();}
}
