package com.chari.chariapp.settings.infrastructure.persistence;

import com.chari.chariapp.settings.domain.SystemSettings;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class SystemSettingsPersistence {
    private final SpringDataSystemSettingsRepository repository;

    public SystemSettingsPersistence(SpringDataSystemSettingsRepository repository) {
        this.repository = repository;
    }

    public SystemSettings get() {
        return repository.findById(1).orElseThrow(() -> new IllegalStateException("System settings row is missing")).toDomain();
    }

    public SystemSettings update(boolean requestsEnabled, boolean notificationsEnabled, String message,
                                 String actorAccountId, Instant now) {
        SystemSettingsJpaEntity entity = repository.findById(1)
                .orElseThrow(() -> new IllegalStateException("System settings row is missing"));
        entity.apply(requestsEnabled, notificationsEnabled, message, actorAccountId, now);
        return repository.save(entity).toDomain();
    }
}
