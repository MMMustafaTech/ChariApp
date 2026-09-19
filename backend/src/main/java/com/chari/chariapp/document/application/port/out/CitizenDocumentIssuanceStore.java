package com.chari.chariapp.document.application.port.out;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.domain.DependentBirthCertificate;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;

import java.time.Instant;
import java.util.UUID;

public interface CitizenDocumentIssuanceStore {
    MyPassport issuePassport(CitizenId citizenId, MyPassport document, AccountId actorId, Instant now);
    MyNationalIdentity issueNationalIdentity(CitizenId citizenId, MyNationalIdentity document, AccountId actorId, Instant now);
    MyBirthCertificate issueBirthCertificate(CitizenId citizenId, MyBirthCertificate document, AccountId actorId, Instant now);
    DependentBirthCertificate issueDependentBirthCertificate(CitizenId parentCitizenId, UUID requestId,
                                                              MyBirthCertificate document, AccountId actorId,
                                                              Instant now);
}
