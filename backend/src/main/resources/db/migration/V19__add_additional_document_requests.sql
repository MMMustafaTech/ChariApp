CREATE TABLE additional_document_requests (
    id CHAR(36) PRIMARY KEY,
    service_type VARCHAR(32) NOT NULL,
    request_id CHAR(36) NOT NULL,
    citizen_id CHAR(36) NOT NULL,
    message VARCHAR(1000),
    status VARCHAR(32) NOT NULL,
    requested_by CHAR(36) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    resolved_by CHAR(36),
    resolved_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_additional_document_request_citizen FOREIGN KEY (citizen_id) REFERENCES citizen_registry(id),
    CONSTRAINT fk_additional_document_request_requested_by FOREIGN KEY (requested_by) REFERENCES accounts(id),
    CONSTRAINT fk_additional_document_request_resolved_by FOREIGN KEY (resolved_by) REFERENCES accounts(id)
);

CREATE INDEX idx_additional_document_request_original
    ON additional_document_requests(service_type, request_id, status);
CREATE INDEX idx_additional_document_request_citizen
    ON additional_document_requests(citizen_id, requested_at);

CREATE TABLE additional_document_request_items (
    additional_request_id CHAR(36) NOT NULL,
    document_name VARCHAR(160) NOT NULL,
    PRIMARY KEY (additional_request_id, document_name),
    CONSTRAINT fk_additional_document_item_request
        FOREIGN KEY (additional_request_id) REFERENCES additional_document_requests(id)
);

CREATE TABLE additional_document_attachments (
    id CHAR(36) PRIMARY KEY,
    additional_request_id CHAR(36) NOT NULL,
    document_name VARCHAR(160) NOT NULL,
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    uploaded_by CHAR(36) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_additional_document_attachment_request
        FOREIGN KEY (additional_request_id) REFERENCES additional_document_requests(id),
    CONSTRAINT fk_additional_document_attachment_uploader
        FOREIGN KEY (uploaded_by) REFERENCES accounts(id)
);

CREATE INDEX idx_additional_document_attachment_request
    ON additional_document_attachments(additional_request_id, document_name);
