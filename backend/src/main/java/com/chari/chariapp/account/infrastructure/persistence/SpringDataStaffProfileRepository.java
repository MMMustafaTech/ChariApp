package com.chari.chariapp.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStaffProfileRepository extends JpaRepository<StaffProfileJpaEntity, String> {
}
