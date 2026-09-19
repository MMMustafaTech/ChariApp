package com.chari.chariapp.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NormalizedCitizenDocumentRepositoryTests {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private NormalizedCitizenDocumentRepository documents;

    @Test
    void assemblesAllLegacyResponsesFromOnePersonAndDocumentSpecificTables() {
        String nationalId = "NAT-" + UUID.randomUUID();
        long child = person(nationalId, "Amina", "Hassan", "Female", LocalDate.of(2002, 5, 11),
                "N'Djamena", "Chadian", "Engineer", "A+", "Address");
        long father = person(null, "Hassan Adam", null, null, LocalDate.of(1973, 2, 2),
                "Mao", null, "Engineer", null, null);
        long mother = person(null, "Mariam Hassan", null, null, LocalDate.of(1978, 7, 7),
                "N'Djamena", null, "Nurse", null, null);

        jdbc.update("INSERT INTO person_parent_relationships(child_person_id,parent_person_id,relationship_type) VALUES (?,?,?)",
                child, father, "FATHER");
        jdbc.update("INSERT INTO person_parent_relationships(child_person_id,parent_person_id,relationship_type) VALUES (?,?,?)",
                child, mother, "MOTHER");
        jdbc.update("""
                INSERT INTO national_identities
                    (person_id,card_serial,place_of_issue,date_of_issue,date_of_expiry)
                VALUES (?,?,?,?,?)
                """, child, "CARD-1", "N'Djamena", Date.valueOf("2024-01-01"), Date.valueOf("2034-01-01"));
        jdbc.update("""
                INSERT INTO passports
                    (person_id,passport_number,place_of_issue,issuing_authority,date_of_issue,date_of_expiry)
                VALUES (?,?,?,?,?,?)
                """, child, "PASS-" + UUID.randomUUID(), "N'Djamena", "DG Police",
                Date.valueOf("2025-01-01"), Date.valueOf("2035-01-01"));
        jdbc.update("""
                INSERT INTO birth_certificate
                    (person_id,certificate_number,declaration_date,created_at)
                VALUES (?,?,?,CURRENT_TIMESTAMP)
                """, child, "CERT-" + UUID.randomUUID(), Date.valueOf("2002-05-12"));

        var identity = documents.nationalIdentity(nationalId).orElseThrow();
        var passport = documents.passport(nationalId).orElseThrow();
        var birth = documents.birthCertificate(nationalId).orElseThrow();

        assertThat(identity.getFirstName()).isEqualTo("Amina");
        assertThat(identity.getFatherName()).isEqualTo("Hassan Adam");
        assertThat(passport.getFirstName()).isEqualTo("Amina");
        assertThat(passport.getProfession()).isEqualTo("Engineer");
        assertThat(birth.getFullName()).isEqualTo("Amina Hassan");
        assertThat(birth.getMotherProfession()).isEqualTo("Nurse");
    }

    private long person(String nationalId, String firstName, String lastName, String gender, LocalDate birthDate,
                        String birthPlace, String nationality, String profession, String bloodGroup, String address) {
        jdbc.update("""
                INSERT INTO person_records
                    (national_id_number,first_name,last_name,gender,date_of_birth,place_of_birth,
                     nationality,profession,blood_group,address,legacy_source_key)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """, nationalId, firstName, lastName, gender, Date.valueOf(birthDate), birthPlace,
                nationality, profession, bloodGroup, address, "TEST:" + UUID.randomUUID());
        return jdbc.queryForObject("SELECT MAX(id) FROM person_records", Long.class);
    }
}
