ALTER TABLE citizens RENAME TO citizens_legacy;
ALTER TABLE national_identities RENAME TO national_identities_legacy;
ALTER TABLE passports RENAME TO passports_legacy;
ALTER TABLE birth_certificate RENAME TO birth_certificate_legacy;

CREATE VIEW citizens AS
SELECT person.id,
       CASE
           WHEN person.last_name IS NULL OR person.last_name = '' THEN person.first_name
           ELSE CONCAT(person.first_name, ' ', person.last_name)
       END AS name,
       father.first_name AS father_name,
       mother.first_name AS mother_name,
       person.date_of_birth,
       person.gender AS sex
FROM person_records person
LEFT JOIN person_parent_relationships father_link
       ON father_link.child_person_id = person.id AND father_link.relationship_type = 'FATHER'
LEFT JOIN person_records father ON father.id = father_link.parent_person_id
LEFT JOIN person_parent_relationships mother_link
       ON mother_link.child_person_id = person.id AND mother_link.relationship_type = 'MOTHER'
LEFT JOIN person_records mother ON mother.id = mother_link.parent_person_id
WHERE person.national_id_number IS NOT NULL;

CREATE VIEW national_identities AS
SELECT document.id,
       person.national_id_number AS national_id_Number,
       person.first_name AS name,
       father.first_name AS father_name,
       mother.first_name AS mother_name,
       person.last_name,
       person.nationality,
       document.card_serial,
       person.gender,
       person.date_of_birth,
       person.place_of_birth,
       document.place_of_issue,
       document.date_of_issue,
       document.date_of_expiry,
       person.blood_group,
       person.profession,
       person.address
FROM normalized_national_identity_documents document
JOIN person_records person ON person.id = document.person_id
LEFT JOIN person_parent_relationships father_link
       ON father_link.child_person_id = person.id AND father_link.relationship_type = 'FATHER'
LEFT JOIN person_records father ON father.id = father_link.parent_person_id
LEFT JOIN person_parent_relationships mother_link
       ON mother_link.child_person_id = person.id AND mother_link.relationship_type = 'MOTHER'
LEFT JOIN person_records mother ON mother.id = mother_link.parent_person_id;

CREATE VIEW passports AS
SELECT document.id,
       person.first_name AS name,
       person.last_name,
       mother.first_name AS mother_name,
       person.nationality,
       person.date_of_birth,
       person.place_of_birth,
       document.date_of_issue,
       document.date_of_expiry,
       person.national_id_number,
       person.gender AS sex,
       person.profession AS job,
       document.place_of_issue,
       document.issuing_authority,
       document.passport_number
FROM normalized_passport_documents document
JOIN person_records person ON person.id = document.person_id
LEFT JOIN person_parent_relationships mother_link
       ON mother_link.child_person_id = person.id AND mother_link.relationship_type = 'MOTHER'
LEFT JOIN person_records mother ON mother.id = mother_link.parent_person_id;

CREATE VIEW birth_certificate AS
SELECT document.id,
       person.national_id_number AS national_id,
       document.certificate_number,
       CASE
           WHEN person.last_name IS NULL OR person.last_name = '' THEN person.first_name
           ELSE CONCAT(person.first_name, ' ', person.last_name)
       END AS full_name,
       person.gender,
       person.date_of_birth AS birth_date,
       person.place_of_birth AS birth_place,
       father.first_name AS father_name,
       father.date_of_birth AS father_birth_date,
       father.place_of_birth AS father_birth_place,
       father.profession AS father_profession,
       mother.first_name AS mother_name,
       mother.date_of_birth AS mother_birth_date,
       mother.place_of_birth AS mother_birth_place,
       mother.profession AS mother_profession,
       document.declaration_date,
       person.address,
       document.created_at
FROM normalized_birth_certificate_documents document
JOIN person_records person ON person.id = document.person_id
LEFT JOIN person_parent_relationships father_link
       ON father_link.child_person_id = person.id AND father_link.relationship_type = 'FATHER'
LEFT JOIN person_records father ON father.id = father_link.parent_person_id
LEFT JOIN person_parent_relationships mother_link
       ON mother_link.child_person_id = person.id AND mother_link.relationship_type = 'MOTHER'
LEFT JOIN person_records mother ON mother.id = mother_link.parent_person_id;
