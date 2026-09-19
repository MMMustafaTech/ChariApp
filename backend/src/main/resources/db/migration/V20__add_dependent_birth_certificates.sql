CREATE TABLE dependent_birth_certificate_documents (
    id CHAR(36) PRIMARY KEY,
    request_id CHAR(36) NOT NULL UNIQUE,
    parent_citizen_id CHAR(36) NOT NULL,
    certificate_number_lookup CHAR(64) NOT NULL UNIQUE,
    encrypted_payload TEXT NOT NULL,
    created_by CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_dependent_birth_parent
        FOREIGN KEY (parent_citizen_id) REFERENCES citizen_registry(id),
    CONSTRAINT fk_dependent_birth_creator
        FOREIGN KEY (created_by) REFERENCES accounts(id)
);

CREATE INDEX idx_dependent_birth_parent_created
    ON dependent_birth_certificate_documents(parent_citizen_id, created_at);
