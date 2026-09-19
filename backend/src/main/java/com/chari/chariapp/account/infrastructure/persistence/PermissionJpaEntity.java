package com.chari.chariapp.account.infrastructure.persistence;

import com.chari.chariapp.account.domain.StaffPermission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions")
public class PermissionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 64)
    private StaffPermission code;

    @Column(nullable = false, length = 255)
    private String description;

    protected PermissionJpaEntity() {
    }

    public StaffPermission getCode() {
        return code;
    }
}
