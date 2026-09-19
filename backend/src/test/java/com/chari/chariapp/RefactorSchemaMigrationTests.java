package com.chari.chariapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RefactorSchemaMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsTheThirdNormalFormSchemaWithoutRemovingLegacyTables() {
        List<String> tableNames = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'PUBLIC'",
                String.class
        );

        assertThat(tableNames).contains(
                "USERS",
                "CITIZEN_REGISTRY",
                "ACCOUNTS",
                "NATIONAL_IDENTITY_DOCUMENTS",
                "PASSPORT_DOCUMENTS",
                "BIRTH_CERTIFICATE_DOCUMENTS",
                "SERVICE_REQUESTS",
                "AUDIT_EVENTS",
                "PERSON_RECORDS",
                "PERSON_PARENT_RELATIONSHIPS",
                "NATIONAL_IDENTITIES",
                "PASSPORTS",
                "BIRTH_CERTIFICATE",
                "CITIZENS_LEGACY",
                "NATIONAL_IDENTITIES_LEGACY",
                "PASSPORTS_LEGACY",
                "BIRTH_CERTIFICATE_LEGACY"
        );

        assertThat(columnsOf("NATIONAL_IDENTITIES")).containsExactlyInAnyOrder(
                "ID", "PERSON_ID", "CARD_SERIAL", "PLACE_OF_ISSUE", "DATE_OF_ISSUE", "DATE_OF_EXPIRY"
        );
        assertThat(columnsOf("PASSPORTS")).containsExactlyInAnyOrder(
                "ID", "PERSON_ID", "PASSPORT_NUMBER", "PLACE_OF_ISSUE", "ISSUING_AUTHORITY",
                "DATE_OF_ISSUE", "DATE_OF_EXPIRY"
        );
        assertThat(columnsOf("BIRTH_CERTIFICATE")).containsExactlyInAnyOrder(
                "ID", "PERSON_ID", "CERTIFICATE_NUMBER", "DECLARATION_DATE", "CREATED_AT"
        );
        assertThat(tableNames).doesNotContain(
                "NORMALIZED_NATIONAL_IDENTITY_DOCUMENTS",
                "NORMALIZED_PASSPORT_DOCUMENTS",
                "NORMALIZED_BIRTH_CERTIFICATE_DOCUMENTS"
        );
    }

    private List<String> columnsOf(String tableName) {
        return jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_schema = 'PUBLIC' AND table_name = ?",
                String.class,
                tableName
        );
    }
}
