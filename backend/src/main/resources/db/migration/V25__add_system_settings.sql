CREATE TABLE system_settings (
    id INT PRIMARY KEY,
    request_submissions_enabled BOOLEAN NOT NULL,
    notifications_enabled BOOLEAN NOT NULL,
    maintenance_message VARCHAR(500),
    updated_by CHAR(36),
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_system_settings_updated_by FOREIGN KEY (updated_by) REFERENCES accounts(id)
);

INSERT INTO system_settings (
    id, request_submissions_enabled, notifications_enabled, maintenance_message, updated_by, updated_at, version
) VALUES (1, TRUE, TRUE, NULL, NULL, CURRENT_TIMESTAMP, 0);
