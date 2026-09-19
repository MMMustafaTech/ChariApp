package com.chari.chariapp.repository;

import com.chari.chariapp.dto.BirthCertificateResponse;
import com.chari.chariapp.dto.NationalIdResponse;
import com.chari.chariapp.dto.PassportResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Read model for normalized legacy lookup endpoints. */
@Repository
public class NormalizedCitizenDocumentRepository {
    private final JdbcClient jdbc;

    public NormalizedCitizenDocumentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public boolean nationalIdentityExists(String nationalId) {
        return jdbc.sql("""
                SELECT COUNT(*) FROM normalized_national_identity_documents document
                JOIN person_records person ON person.id = document.person_id
                WHERE person.national_id_number = :nationalId
                """).param("nationalId", nationalId).query(Long.class).single() > 0;
    }

    public Optional<NationalIdResponse> nationalIdentity(String nationalId) {
        return jdbc.sql("""
                SELECT person.national_id_number, person.first_name, person.last_name, person.gender,
                       person.place_of_birth, person.date_of_birth, document.card_serial,
                       document.place_of_issue, document.date_of_issue, document.date_of_expiry,
                       person.profession, father.first_name AS father_name,
                       mother.first_name AS mother_name, person.address, person.blood_group
                FROM normalized_national_identity_documents document
                JOIN person_records person ON person.id = document.person_id
                LEFT JOIN person_parent_relationships father_link
                       ON father_link.child_person_id = person.id AND father_link.relationship_type = 'FATHER'
                LEFT JOIN person_records father ON father.id = father_link.parent_person_id
                LEFT JOIN person_parent_relationships mother_link
                       ON mother_link.child_person_id = person.id AND mother_link.relationship_type = 'MOTHER'
                LEFT JOIN person_records mother ON mother.id = mother_link.parent_person_id
                WHERE person.national_id_number = :nationalId
                """).param("nationalId", nationalId).query((rs, row) -> {
                    NationalIdResponse response = new NationalIdResponse();
                    response.setNationalId(rs.getString("national_id_number"));
                    response.setFirstName(rs.getString("first_name"));
                    response.setLastName(rs.getString("last_name"));
                    response.setGender(rs.getString("gender"));
                    response.setPlaceOfBirth(rs.getString("place_of_birth"));
                    response.setDateofBirth(string(rs.getObject("date_of_birth")));
                    response.setCardSerial(rs.getString("card_serial"));
                    response.setIssueDetails(issueDetails(rs.getString("place_of_issue"), rs.getObject("date_of_issue")));
                    response.setDateOfExpiry(string(rs.getObject("date_of_expiry")));
                    response.setProfession(rs.getString("profession"));
                    response.setFatherName(rs.getString("father_name"));
                    response.setMotherName(rs.getString("mother_name"));
                    response.setAddress(rs.getString("address"));
                    response.setBloodGroup(rs.getString("blood_group"));
                    return response;
                }).optional();
    }

    public Optional<PassportResponse> passport(String nationalId) {
        return jdbc.sql("""
                SELECT document.passport_number, person.first_name, person.last_name,
                       person.date_of_birth, person.place_of_birth, document.date_of_issue,
                       document.date_of_expiry, document.place_of_issue, document.issuing_authority,
                       person.profession, person.nationality, person.gender
                FROM normalized_passport_documents document
                JOIN person_records person ON person.id = document.person_id
                WHERE person.national_id_number = :nationalId
                """).param("nationalId", nationalId).query((rs, row) -> new PassportResponse(
                        rs.getString("passport_number"), rs.getString("first_name"), rs.getString("last_name"),
                        string(rs.getObject("date_of_birth")), rs.getString("place_of_birth"),
                        string(rs.getObject("date_of_issue")), string(rs.getObject("date_of_expiry")),
                        rs.getString("place_of_issue"), rs.getString("issuing_authority"),
                        rs.getString("profession"), rs.getString("nationality"), rs.getString("gender")
                )).optional();
    }

    public Optional<BirthCertificateResponse> birthCertificate(String nationalId) {
        return jdbc.sql("""
                SELECT document.certificate_number, person.first_name, person.last_name, person.gender,
                       person.date_of_birth, person.place_of_birth,
                       father.first_name AS father_name, father.date_of_birth AS father_birth_date,
                       father.place_of_birth AS father_birth_place, father.profession AS father_profession,
                       mother.first_name AS mother_name, mother.date_of_birth AS mother_birth_date,
                       mother.place_of_birth AS mother_birth_place, mother.profession AS mother_profession,
                       document.declaration_date, person.address
                FROM normalized_birth_certificate_documents document
                JOIN person_records person ON person.id = document.person_id
                LEFT JOIN person_parent_relationships father_link
                       ON father_link.child_person_id = person.id AND father_link.relationship_type = 'FATHER'
                LEFT JOIN person_records father ON father.id = father_link.parent_person_id
                LEFT JOIN person_parent_relationships mother_link
                       ON mother_link.child_person_id = person.id AND mother_link.relationship_type = 'MOTHER'
                LEFT JOIN person_records mother ON mother.id = mother_link.parent_person_id
                WHERE person.national_id_number = :nationalId
                ORDER BY document.id DESC
                LIMIT 1
                """).param("nationalId", nationalId).query((rs, row) -> new BirthCertificateResponse(
                        rs.getString("certificate_number"), fullName(rs.getString("first_name"), rs.getString("last_name")),
                        rs.getString("gender"), string(rs.getObject("date_of_birth")), rs.getString("place_of_birth"),
                        rs.getString("father_name"), string(rs.getObject("father_birth_date")),
                        rs.getString("father_birth_place"), rs.getString("father_profession"),
                        rs.getString("mother_name"), string(rs.getObject("mother_birth_date")),
                        rs.getString("mother_birth_place"), rs.getString("mother_profession"),
                        string(rs.getObject("declaration_date")), rs.getString("address")
                )).optional();
    }

    private static String fullName(String firstName, String lastName) {
        return lastName == null || lastName.isBlank() ? firstName : firstName + " " + lastName;
    }

    private static String issueDetails(String place, Object date) {
        if (place == null) return string(date);
        if (date == null) return place;
        return place + "/" + date;
    }

    private static String string(Object value) {
        return value == null ? null : value.toString();
    }
}
