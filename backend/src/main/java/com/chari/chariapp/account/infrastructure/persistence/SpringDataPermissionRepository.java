package com.chari.chariapp.account.infrastructure.persistence;

import com.chari.chariapp.account.domain.StaffPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataPermissionRepository extends JpaRepository<PermissionJpaEntity, Long> {
    List<PermissionJpaEntity> findByCodeIn(Collection<StaffPermission> codes);
}
