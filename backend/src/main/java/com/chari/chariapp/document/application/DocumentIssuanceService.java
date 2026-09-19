package com.chari.chariapp.document.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.birthrequest.application.port.out.NewbornRegistrationDetailsStore;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequest;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequestKind;
import com.chari.chariapp.birthrequest.domain.NewbornRegistrationDetails;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.application.port.out.CitizenDocumentIssuanceStore;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequest;
import com.chari.chariapp.request.domain.PassportRequest;
import com.chari.chariapp.request.domain.RequestBeneficiaryType;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class DocumentIssuanceService {
    private final CitizenDocumentReadStore documents;
    private final CitizenDocumentIssuanceStore issuance;
    private final CitizenStore citizens;
    private final NewbornRegistrationDetailsStore newbornDetails;
    private final PersonalDataProtector protector;
    private final OperationalAuditStore audit;

    public DocumentIssuanceService(CitizenDocumentReadStore documents,
                                   CitizenDocumentIssuanceStore issuance,
                                   CitizenStore citizens,
                                   NewbornRegistrationDetailsStore newbornDetails,
                                   PersonalDataProtector protector,
                                   OperationalAuditStore audit) {
        this.documents = documents;
        this.issuance = issuance;
        this.citizens = citizens;
        this.newbornDetails = newbornDetails;
        this.protector = protector;
        this.audit = audit;
    }

    public MyPassport issue(PassportRequest request, AccountId actorId, Instant now) {
        rejectDependentIssuance(request.submissionDetails());
        LocalDate issuedOn = date(now);
        MyPassport previous = documents.findPassportByCitizenId(request.citizenId()).orElse(null);
        MyNationalIdentity identity = documents.findNationalIdentityByCitizenId(request.citizenId()).orElse(null);
        MyBirthCertificate birth = documents.findBirthCertificateByCitizenId(request.citizenId()).orElse(null);
        if (previous == null && identity == null && birth == null) {
            throw unavailable("Passport issuance requires an identity or birth-certificate source");
        }
        MyPassport document;
        if (previous != null) {
            document = new MyPassport(number("P", issuedOn), previous.firstName(), previous.lastName(),
                    previous.dateOfBirth(), previous.placeOfBirth(), issuedOn, issuedOn.plusYears(10),
                    previous.placeOfIssue(), previous.issuingAuthority(), previous.profession(),
                    previous.nationality(), previous.sex());
        } else if (identity != null) {
            document = new MyPassport(number("P", issuedOn), identity.firstName(), identity.lastName(),
                    identity.dateOfBirth(), identity.placeOfBirth(), issuedOn, issuedOn.plusYears(10),
                    "N'Djamena", "DG de la Police Nationale", identity.profession(), "Chadian",
                    identity.gender());
        } else {
            String[] name = splitName(birth.fullName());
            document = new MyPassport(number("P", issuedOn), name[0], name[1], birth.birthDate(), birth.birthPlace(),
                    issuedOn, issuedOn.plusYears(10), "N'Djamena", "DG de la Police Nationale",
                    null, "Chadian", birth.gender());
        }
        MyPassport saved = issuance.issuePassport(request.citizenId(), document, actorId, now);
        audit(actorId, "PASSPORT_DOCUMENT_ISSUED", request.id(), now);
        return saved;
    }

    public MyNationalIdentity issue(NationalIdentityRequest request, AccountId actorId, Instant now) {
        rejectDependentIssuance(request.submissionDetails());
        LocalDate issuedOn = date(now);
        MyNationalIdentity previous = documents.findNationalIdentityByCitizenId(request.citizenId()).orElse(null);
        MyBirthCertificate birth = documents.findBirthCertificateByCitizenId(request.citizenId()).orElse(null);
        Citizen citizen = citizen(request.citizenId());
        String nationalId = protector.decrypt(citizen.nationalId().ciphertext());
        if (previous == null && birth == null) {
            throw unavailable("National identity issuance requires a birth-certificate source");
        }
        MyNationalIdentity document;
        if (previous != null) {
            document = new MyNationalIdentity(nationalId, previous.firstName(), previous.lastName(),
                    previous.gender(), previous.placeOfBirth(), previous.dateOfBirth(), number("ID", issuedOn),
                    previous.placeOfIssue(), issuedOn, issuedOn.plusYears(10), previous.profession(),
                    previous.fatherName(), previous.motherName(), previous.address(), previous.bloodGroup());
        } else {
            String[] name = splitName(birth.fullName());
            document = new MyNationalIdentity(nationalId, name[0], name[1], birth.gender(),
                    birth.birthPlace(), birth.birthDate(), number("ID", issuedOn), "N'Djamena", issuedOn,
                    issuedOn.plusYears(10), null, birth.fatherName(), birth.motherName(), birth.address(), null);
        }
        MyNationalIdentity saved = issuance.issueNationalIdentity(request.citizenId(), document, actorId, now);
        audit(actorId, "NATIONAL_IDENTITY_DOCUMENT_ISSUED", request.id(), now);
        return saved;
    }

    public MyBirthCertificate issue(BirthCertificateRequest request, AccountId actorId, Instant now) {
        LocalDate issuedOn = date(now);
        if (request.kind() == BirthCertificateRequestKind.NEWBORN_REGISTRATION) {
            NewbornRegistrationDetails details = newbornDetails.findByRequestId(request.id())
                    .orElseThrow(() -> unavailable("Newborn registration details are unavailable"));
            MyBirthCertificate document = new MyBirthCertificate(number("BC", issuedOn),
                    (details.childFirstName() + " " + details.childLastName()).trim(),
                    details.gender().name(), details.dateOfBirth(), details.placeOfBirth(),
                    details.fatherFullName(), null, null, null,
                    details.motherFullName(), null, null, null, issuedOn, null);
            issuance.issueDependentBirthCertificate(request.citizenId(), request.id(), document, actorId, now);
            audit(actorId, "DEPENDENT_BIRTH_CERTIFICATE_ISSUED", request.id(), now);
            return document;
        }
        MyBirthCertificate previous = documents.findBirthCertificateByCitizenId(request.citizenId())
                .orElseThrow(() -> unavailable("Birth-certificate source is unavailable"));
        MyBirthCertificate document = new MyBirthCertificate(number("BC", issuedOn), previous.fullName(),
                previous.gender(), previous.birthDate(), previous.birthPlace(), previous.fatherName(),
                previous.fatherBirthDate(), previous.fatherBirthPlace(), previous.fatherProfession(),
                previous.motherName(), previous.motherBirthDate(), previous.motherBirthPlace(),
                previous.motherProfession(), issuedOn, previous.address());
        MyBirthCertificate saved = issuance.issueBirthCertificate(request.citizenId(), document, actorId, now);
        audit(actorId, "BIRTH_CERTIFICATE_DOCUMENT_ISSUED", request.id(), now);
        return saved;
    }

    private Citizen citizen(CitizenId id) {
        return citizens.findById(id).orElseThrow(() -> new NotFoundException("Citizen not found"));
    }

    private void audit(AccountId actorId, String action, UUID requestId, Instant now) {
        audit.record(actorId.value().toString(), action, "SERVICE_REQUEST", requestId.toString(), null, now);
    }

    private static void rejectDependentIssuance(
            com.chari.chariapp.request.domain.ServiceRequestSubmissionDetails details) {
        if (details != null && details.beneficiaryType() == RequestBeneficiaryType.DEPENDENT_CHILD) {
            throw unavailable("Dependent child document issuance is not enabled yet");
        }
    }

    private static LocalDate date(Instant now) {
        return LocalDate.ofInstant(now, ZoneOffset.UTC);
    }

    private static String number(String prefix, LocalDate issuedOn) {
        return prefix + issuedOn.getYear()
                + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private static String[] splitName(String fullName) {
        String normalized = fullName == null ? "" : fullName.trim();
        if (normalized.isEmpty()) return new String[]{null, null};
        int separator = normalized.indexOf(' ');
        return separator < 0
                ? new String[]{normalized, null}
                : new String[]{normalized.substring(0, separator), normalized.substring(separator + 1).trim()};
    }

    private static DocumentIssuanceException unavailable(String message) {
        return new DocumentIssuanceException("DOCUMENT_ISSUANCE_DATA_UNAVAILABLE", message);
    }
}
