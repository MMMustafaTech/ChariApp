-- V23 kept denormalized compatibility views under the historical table names.
-- That made direct inspection look denormalized even though the stored data was normalized.
-- Retire those views and give the real 3NF document tables the canonical names.
DROP VIEW citizens;
DROP VIEW national_identities;
DROP VIEW passports;
DROP VIEW birth_certificate;

ALTER TABLE normalized_national_identity_documents RENAME TO national_identities;
ALTER TABLE normalized_passport_documents RENAME TO passports;
ALTER TABLE normalized_birth_certificate_documents RENAME TO birth_certificate;
