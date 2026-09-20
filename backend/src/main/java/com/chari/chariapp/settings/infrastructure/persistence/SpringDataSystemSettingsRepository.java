package com.chari.chariapp.settings.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataSystemSettingsRepository extends JpaRepository<SystemSettingsJpaEntity, Integer> {
}
