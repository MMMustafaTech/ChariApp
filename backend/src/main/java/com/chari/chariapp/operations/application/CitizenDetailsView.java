package com.chari.chariapp.operations.application;

import com.chari.chariapp.appointment.domain.Appointment;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.document.domain.DependentBirthCertificate;

import java.util.List;

public record CitizenDetailsView(
        CitizenSummaryView citizen,
        MyNationalIdentity nationalIdentity,
        MyPassport passport,
        MyBirthCertificate birthCertificate,
        List<DependentBirthCertificate> dependentBirthCertificates,
        List<UnifiedServiceRequestView> requests,
        List<Appointment> appointments
) {
    public CitizenDetailsView {
        dependentBirthCertificates = List.copyOf(dependentBirthCertificates);
        requests = List.copyOf(requests);
        appointments = List.copyOf(appointments);
    }
}
