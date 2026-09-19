CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(96) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE account_permissions (
    account_id CHAR(36) NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by CHAR(36),
    PRIMARY KEY (account_id, permission_id),
    CONSTRAINT fk_account_permissions_account
        FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_account_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(id),
    CONSTRAINT fk_account_permissions_granted_by
        FOREIGN KEY (granted_by) REFERENCES accounts(id)
);

CREATE TABLE staff_profiles (
    account_id CHAR(36) PRIMARY KEY,
    employee_number VARCHAR(32) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_lookup CHAR(64) UNIQUE,
    phone_ciphertext TEXT,
    job_title VARCHAR(120),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_staff_profiles_account
        FOREIGN KEY (account_id) REFERENCES accounts(id)
);

INSERT INTO permissions (code, description) VALUES
    ('DASHBOARD_VIEW', 'View the operational dashboard'),
    ('REQUEST_VIEW', 'View service requests'),
    ('REQUEST_REVIEW', 'Start reviewing a service request'),
    ('REQUEST_APPROVE', 'Approve a service request'),
    ('REQUEST_REJECT', 'Reject a service request'),
    ('REQUEST_COMPLETE', 'Complete an approved service request'),
    ('REQUEST_ADDITIONAL_DOCUMENTS', 'Request additional supporting documents'),
    ('CITIZEN_VIEW', 'View citizen records'),
    ('CITIZEN_EDIT', 'Edit permitted citizen fields'),
    ('NATIONAL_ID_VIEW', 'View national identity records'),
    ('NATIONAL_ID_CREATE', 'Create national identity records'),
    ('NATIONAL_ID_EDIT', 'Edit national identity records'),
    ('PASSPORT_VIEW', 'View passport records'),
    ('PASSPORT_CREATE', 'Create passport records'),
    ('PASSPORT_EDIT', 'Edit passport records'),
    ('BIRTH_CERTIFICATE_VIEW', 'View birth certificate records'),
    ('BIRTH_CERTIFICATE_CREATE', 'Create birth certificate records'),
    ('BIRTH_CERTIFICATE_EDIT', 'Edit birth certificate records'),
    ('APPOINTMENT_VIEW', 'View operational appointments'),
    ('APPOINTMENT_MANAGE', 'Manage appointments and availability'),
    ('NOTIFICATION_VIEW', 'View notification delivery records'),
    ('NOTIFICATION_SEND', 'Send notifications'),
    ('STAFF_VIEW', 'View staff accounts'),
    ('STAFF_MANAGE', 'Provision and manage staff accounts'),
    ('REPORT_VIEW', 'View and export operational reports'),
    ('AUDIT_VIEW', 'View operational audit events'),
    ('SETTINGS_MANAGE', 'Manage system settings');

