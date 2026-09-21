package com.chari.chariapp.appointment.domain;

public enum AppointmentServiceType {
    PASSPORT,
    CIVIL_STATUS,
    NATIONAL_IDENTITY,
    BIRTH_CERTIFICATE;

    public boolean isAppointmentDepartment() {
        return this == PASSPORT || this == CIVIL_STATUS;
    }

    public AppointmentServiceType appointmentDepartment() {
        return this == PASSPORT ? PASSPORT : CIVIL_STATUS;
    }

    public boolean belongsToDepartment(AppointmentServiceType department) {
        return appointmentDepartment() == department;
    }
}
