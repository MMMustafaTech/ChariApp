package com.chari.chariapp.notification.infrastructure.persistence;
import com.chari.chariapp.citizen.domain.CitizenId; import com.chari.chariapp.notification.application.port.out.NotificationStore; import com.chari.chariapp.notification.domain.UserNotification; import java.util.*; import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;
@Repository public class JpaNotificationStore implements NotificationStore {
 private final SpringDataNotificationRepository repository; public JpaNotificationStore(SpringDataNotificationRepository repository){this.repository=repository;}
 public UserNotification save(UserNotification n){return repository.findById(n.id().toString()).map(e->{e.apply(n);return repository.save(e);}).orElseGet(()->repository.save(UserNotificationJpaEntity.from(n))).toDomain();}
 public Optional<UserNotification> findById(UUID id){return repository.findById(id.toString()).map(UserNotificationJpaEntity::toDomain);}
 public List<UserNotification> findAll(){return repository.findAll(Sort.by(Sort.Direction.DESC,"createdAt")).stream().map(UserNotificationJpaEntity::toDomain).toList();}
 public List<UserNotification> findByCitizenId(CitizenId c){return repository.findByCitizenIdOrderByCreatedAtDesc(c.value().toString()).stream().map(UserNotificationJpaEntity::toDomain).toList();}
 public List<UserNotification> findUnreadByCitizenId(CitizenId c){return repository.findByCitizenIdAndReadAtIsNullOrderByCreatedAtDesc(c.value().toString()).stream().map(UserNotificationJpaEntity::toDomain).toList();}
 public long countUnreadByCitizenId(CitizenId c){return repository.countByCitizenIdAndReadAtIsNull(c.value().toString());}
}
