CREATE TABLE person_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    national_id_number VARCHAR(255) UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    gender VARCHAR(32),
    date_of_birth DATE,
    place_of_birth VARCHAR(255),
    nationality VARCHAR(255),
    profession VARCHAR(255),
    blood_group VARCHAR(32),
    address VARCHAR(500),
    legacy_source_key VARCHAR(100) UNIQUE
);

CREATE TABLE person_parent_relationships (
    child_person_id BIGINT NOT NULL,
    parent_person_id BIGINT NOT NULL,
    relationship_type VARCHAR(16) NOT NULL,
    PRIMARY KEY (child_person_id, relationship_type),
    CONSTRAINT fk_person_parent_child FOREIGN KEY (child_person_id) REFERENCES person_records(id),
    CONSTRAINT fk_person_parent_parent FOREIGN KEY (parent_person_id) REFERENCES person_records(id),
    CONSTRAINT ck_person_parent_type CHECK (relationship_type IN ('FATHER', 'MOTHER'))
);

CREATE TABLE normalized_national_identity_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_id BIGINT NOT NULL UNIQUE,
    card_serial VARCHAR(255),
    place_of_issue VARCHAR(255),
    date_of_issue DATE,
    date_of_expiry DATE,
    CONSTRAINT fk_normalized_identity_person FOREIGN KEY (person_id) REFERENCES person_records(id)
);

CREATE TABLE normalized_passport_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_id BIGINT NOT NULL UNIQUE,
    passport_number VARCHAR(255) NOT NULL UNIQUE,
    place_of_issue VARCHAR(255),
    issuing_authority VARCHAR(255),
    date_of_issue DATE,
    date_of_expiry DATE,
    CONSTRAINT fk_normalized_passport_person FOREIGN KEY (person_id) REFERENCES person_records(id)
);

CREATE TABLE normalized_birth_certificate_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_id BIGINT NOT NULL,
    certificate_number VARCHAR(255) NOT NULL UNIQUE,
    declaration_date DATE,
    created_at TIMESTAMP,
    CONSTRAINT fk_normalized_birth_person FOREIGN KEY (person_id) REFERENCES person_records(id)
);

-- National identity is the most complete legacy source, so it establishes the canonical person row.
INSERT INTO person_records (
    national_id_number, first_name, last_name, gender, date_of_birth, place_of_birth,
    nationality, profession, blood_group, address, legacy_source_key
)
SELECT national_id_Number, COALESCE(NULLIF(name, ''), national_id_Number), last_name, gender,
       date_of_birth, place_of_birth, nationality, profession, blood_group, address,
       CONCAT('NATIONAL_ID:', id)
FROM national_identities;

-- Add citizens that only exist in a birth certificate.
INSERT INTO person_records (
    national_id_number, first_name, gender, date_of_birth, place_of_birth, address, legacy_source_key
)
SELECT bc.national_id, COALESCE(NULLIF(bc.full_name, ''), bc.national_id), bc.gender,
       bc.birth_date, bc.birth_place, bc.address, CONCAT('BIRTH:', bc.id, ':CHILD')
FROM birth_certificate bc
WHERE bc.national_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM person_records p WHERE p.national_id_number = bc.national_id);

-- Add citizens that only exist in a passport.
INSERT INTO person_records (
    national_id_number, first_name, last_name, gender, date_of_birth, place_of_birth,
    nationality, profession, legacy_source_key
)
SELECT pp.national_id_number, COALESCE(NULLIF(pp.name, ''), pp.national_id_number), pp.last_name,
       pp.sex, pp.date_of_birth, pp.place_of_birth, pp.nationality, pp.job,
       CONCAT('PASSPORT:', pp.id)
FROM passports pp
WHERE pp.national_id_number IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM person_records p WHERE p.national_id_number = pp.national_id_number);

-- Parents without a reliable national identifier remain distinct people linked to their child.
INSERT INTO person_records (first_name, date_of_birth, place_of_birth, profession, legacy_source_key)
SELECT COALESCE(NULLIF(father_name, ''), 'Unknown'), father_birth_date, father_birth_place,
       father_profession, CONCAT('BIRTH:', id, ':FATHER')
FROM birth_certificate bc
JOIN (
    SELECT national_id, MIN(id) AS selected_id
    FROM birth_certificate
    WHERE national_id IS NOT NULL
    GROUP BY national_id
) selected ON selected.selected_id = bc.id;

INSERT INTO person_records (first_name, date_of_birth, place_of_birth, profession, legacy_source_key)
SELECT COALESCE(NULLIF(mother_name, ''), 'Unknown'), mother_birth_date, mother_birth_place,
       mother_profession, CONCAT('BIRTH:', id, ':MOTHER')
FROM birth_certificate bc
JOIN (
    SELECT national_id, MIN(id) AS selected_id
    FROM birth_certificate
    WHERE national_id IS NOT NULL
    GROUP BY national_id
) selected ON selected.selected_id = bc.id;

INSERT INTO person_parent_relationships (child_person_id, parent_person_id, relationship_type)
SELECT child.id, parent.id, 'FATHER'
FROM birth_certificate bc
JOIN (
    SELECT national_id, MIN(id) AS selected_id
    FROM birth_certificate
    WHERE national_id IS NOT NULL
    GROUP BY national_id
) selected ON selected.selected_id = bc.id
JOIN person_records child ON child.national_id_number = bc.national_id
JOIN person_records parent ON parent.legacy_source_key = CONCAT('BIRTH:', bc.id, ':FATHER');

INSERT INTO person_parent_relationships (child_person_id, parent_person_id, relationship_type)
SELECT child.id, parent.id, 'MOTHER'
FROM birth_certificate bc
JOIN (
    SELECT national_id, MIN(id) AS selected_id
    FROM birth_certificate
    WHERE national_id IS NOT NULL
    GROUP BY national_id
) selected ON selected.selected_id = bc.id
JOIN person_records child ON child.national_id_number = bc.national_id
JOIN person_records parent ON parent.legacy_source_key = CONCAT('BIRTH:', bc.id, ':MOTHER');

INSERT INTO normalized_national_identity_documents (
    id, person_id, card_serial, place_of_issue, date_of_issue, date_of_expiry
)
SELECT ni.id, person.id, ni.card_serial, ni.place_of_issue, ni.date_of_issue, ni.date_of_expiry
FROM national_identities ni
JOIN person_records person ON person.national_id_number = ni.national_id_Number;

INSERT INTO normalized_passport_documents (
    id, person_id, passport_number, place_of_issue, issuing_authority, date_of_issue, date_of_expiry
)
SELECT pp.id, person.id, pp.passport_number, pp.place_of_issue, pp.issuing_authority,
       pp.date_of_issue, pp.date_of_expiry
FROM passports pp
JOIN person_records person ON person.national_id_number = pp.national_id_number
WHERE pp.passport_number IS NOT NULL;

INSERT INTO normalized_birth_certificate_documents (
    id, person_id, certificate_number, declaration_date, created_at
)
SELECT bc.id, person.id, bc.certificate_number, bc.declaration_date, bc.created_at
FROM birth_certificate bc
JOIN person_records person ON person.national_id_number = bc.national_id
WHERE bc.certificate_number IS NOT NULL;

CREATE INDEX idx_person_parent_parent ON person_parent_relationships(parent_person_id);
CREATE INDEX idx_person_records_national_id ON person_records(national_id_number);
