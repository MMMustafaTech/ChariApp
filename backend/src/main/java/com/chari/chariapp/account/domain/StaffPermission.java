package com.chari.chariapp.account.domain;

import java.util.EnumSet;
import java.util.Set;

public enum StaffPermission {
    DASHBOARD_VIEW,
    REQUEST_VIEW,
    REQUEST_REVIEW,
    REQUEST_APPROVE,
    REQUEST_REJECT,
    REQUEST_COMPLETE,
    REQUEST_ADDITIONAL_DOCUMENTS,
    CITIZEN_VIEW,
    CITIZEN_EDIT,
    NATIONAL_ID_VIEW,
    NATIONAL_ID_CREATE,
    NATIONAL_ID_EDIT,
    PASSPORT_VIEW,
    PASSPORT_CREATE,
    PASSPORT_EDIT,
    BIRTH_CERTIFICATE_VIEW,
    BIRTH_CERTIFICATE_CREATE,
    BIRTH_CERTIFICATE_EDIT,
    APPOINTMENT_VIEW,
    APPOINTMENT_MANAGE,
    NOTIFICATION_VIEW,
    NOTIFICATION_SEND,
    STAFF_VIEW,
    STAFF_MANAGE,
    REPORT_VIEW,
    AUDIT_VIEW,
    SETTINGS_MANAGE;

    public static Set<StaffPermission> administratorDefaults() {
        return Set.copyOf(EnumSet.allOf(StaffPermission.class));
    }

    public static Set<StaffPermission> employeeDefaults() {
        EnumSet<StaffPermission> permissions = EnumSet.allOf(StaffPermission.class);
        permissions.remove(STAFF_VIEW);
        permissions.remove(STAFF_MANAGE);
        permissions.remove(REPORT_VIEW);
        permissions.remove(AUDIT_VIEW);
        permissions.remove(SETTINGS_MANAGE);
        return Set.copyOf(permissions);
    }
}

