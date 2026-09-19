package com.chari.chariapp.account.infrastructure.persistence;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.StaffProfile;
import com.chari.chariapp.citizen.domain.PhoneReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "staff_profiles")
public class StaffProfileJpaEntity {
    @Id
    @Column(name = "account_id", length = 36, nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private String accountId;

    @Column(name = "employee_number", length = 32, nullable = false, unique = true)
    private String employeeNumber;
    @Column(name = "first_name", length = 100)
    private String firstName;
    @Column(name = "last_name", length = 100)
    private String lastName;
    @Column(name = "phone_lookup", length = 64, unique = true, columnDefinition = "CHAR(64)")
    private String phoneLookup;
    @Column(name = "phone_ciphertext", columnDefinition = "TEXT")
    private String phoneCiphertext;
    @Column(name = "job_title", length = 120)
    private String jobTitle;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    private Long version;

    protected StaffProfileJpaEntity() {
    }

    static StaffProfileJpaEntity fromDomain(StaffProfile profile) {
        StaffProfileJpaEntity entity = new StaffProfileJpaEntity();
        entity.accountId = profile.accountId().value().toString();
        entity.apply(profile);
        entity.createdAt = profile.createdAt();
        return entity;
    }

    void apply(StaffProfile profile) {
        employeeNumber = profile.employeeNumber();
        firstName = profile.firstName();
        lastName = profile.lastName();
        phoneLookup = profile.phone() == null ? null : profile.phone().lookup();
        phoneCiphertext = profile.phone() == null ? null : profile.phone().ciphertext();
        jobTitle = profile.jobTitle();
        updatedAt = profile.updatedAt();
    }

    StaffProfile toDomain() {
        PhoneReference phone = phoneLookup == null ? null : new PhoneReference(phoneLookup, phoneCiphertext);
        return new StaffProfile(new AccountId(UUID.fromString(accountId)), employeeNumber, firstName, lastName,
                phone, jobTitle, createdAt, updatedAt);
    }
}
