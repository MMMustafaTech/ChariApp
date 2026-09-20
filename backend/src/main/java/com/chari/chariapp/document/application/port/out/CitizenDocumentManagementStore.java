package com.chari.chariapp.document.application.port.out;

import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;

public interface CitizenDocumentManagementStore {
    MyPassport updatePassport(CitizenId citizenId, MyPassport document);
    MyNationalIdentity updateNationalIdentity(CitizenId citizenId, MyNationalIdentity document);
    MyBirthCertificate updateBirthCertificate(CitizenId citizenId, MyBirthCertificate document);
}
