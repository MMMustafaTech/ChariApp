package com.chari.chariapp.shared.infrastructure.persistence;

import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.application.OperationalAuditEventView;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class JpaOperationalAuditStore implements OperationalAuditStore {
    private final SpringDataOperationalAuditEventRepository repository;

    public JpaOperationalAuditStore(SpringDataOperationalAuditEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void record(String actorAccountId, String action, String targetType, String targetId, String metadata, Instant occurredAt) {
        repository.save(OperationalAuditEventJpaEntity.success(actorAccountId, action, targetType, targetId, metadata, occurredAt));
    }

    @Override
    public List<OperationalAuditEventView> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "occurredAt")).stream()
                .map(OperationalAuditEventJpaEntity::toView)
                .toList();
    }
}
