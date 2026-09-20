package com.chari.chariapp.citizen.infrastructure.migration;

import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.shared.security.PersonalDataNormalizer;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CitizenRegistryBackfillServiceTests {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private CitizenStore citizenStore;
    @Autowired private CitizenRegistryBackfillService backfillService;
    @Autowired private PersonalDataProtector dataProtector;

    @Test
    void createsAnEncryptedRegistryCitizenWithoutInventingAPhoneAndIsIdempotent() {
        String legacyNationalId = "NAT-" + UUID.randomUUID();
        jdbc.update("""
                INSERT INTO person_records
                    (national_id_number, first_name, legacy_source_key)
                VALUES (?, ?, ?)
                """, legacyNationalId, "Migration Test", "TEST:" + UUID.randomUUID());

        CitizenRegistryBackfillService.BackfillReport first = backfillService.backfillAll();
        String lookup = dataProtector.lookup(PersonalDataNormalizer.nationalId(legacyNationalId));
        var migratedCitizen = citizenStore.findByNationalIdLookup(lookup).orElseThrow();

        assertThat(first.citizensCreated()).isPositive();
        assertThat(migratedCitizen.nationalId().ciphertext()).doesNotContain(legacyNationalId);
        assertThat(migratedCitizen.verifiedPhoneOptional()).isEmpty();

        CitizenRegistryBackfillService.BackfillReport second = backfillService.backfillAll();
        assertThat(second.citizensCreated()).isZero();
        assertThat(second.citizensAlreadyPresent()).isPositive();
    }
}
