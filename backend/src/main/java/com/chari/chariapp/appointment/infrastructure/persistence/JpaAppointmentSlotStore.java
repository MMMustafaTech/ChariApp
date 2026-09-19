package com.chari.chariapp.appointment.infrastructure.persistence;
import com.chari.chariapp.appointment.application.port.out.AppointmentSlotStore;
import com.chari.chariapp.appointment.domain.*;
import java.time.Instant; import java.util.*; import org.springframework.stereotype.Repository;
@Repository public class JpaAppointmentSlotStore implements AppointmentSlotStore {
 private final SpringDataAppointmentSlotRepository repository; public JpaAppointmentSlotStore(SpringDataAppointmentSlotRepository repository){this.repository=repository;}
 public Optional<AppointmentSlot> findByIdForUpdate(UUID id){return repository.findForUpdate(id.toString()).map(AppointmentSlotJpaEntity::toDomain);}
 public AppointmentSlot save(AppointmentSlot slot){return repository.findById(slot.id().toString()).map(e->{e.apply(slot);return repository.save(e);}).orElseGet(()->repository.save(AppointmentSlotJpaEntity.from(slot))).toDomain();}
 public List<AppointmentSlot> findAvailable(AppointmentServiceType type,Instant now){return repository.findByServiceTypeAndActiveTrueAndStartsAtAfterOrderByStartsAtAsc(type,now).stream().map(AppointmentSlotJpaEntity::toDomain).filter(s->s.availableAt(now)).toList();}
 public List<AppointmentSlot> findAll(){return repository.findAll().stream().map(AppointmentSlotJpaEntity::toDomain).toList();}
}
