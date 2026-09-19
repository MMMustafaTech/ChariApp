ALTER TABLE request_attachments
    ADD COLUMN document_type VARCHAR(64) NOT NULL DEFAULT 'OTHER_SUPPORTING_DOCUMENT';

CREATE INDEX idx_request_attachments_request_type
    ON request_attachments(service_request_id, document_type);
