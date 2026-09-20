package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.application.DocumentAlreadyExistsException;
import com.chari.chariapp.document.application.DocumentNotFoundException;
import com.chari.chariapp.document.application.port.out.CitizenDocumentIssuanceStore;
import com.chari.chariapp.document.application.port.out.CitizenDocumentManagementStore;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class OperationsDocumentService {
    private final PassportRequestActorAccess access;
    private final CitizenStore citizens;
    private final CitizenDocumentReadStore documents;
    private final CitizenDocumentIssuanceStore issuance;
    private final CitizenDocumentManagementStore management;
    private final PersonalDataProtector protector;
    private final OperationalAuditStore audit;
    private final Clock clock;

    public OperationsDocumentService(PassportRequestActorAccess access, CitizenStore citizens,
                                     CitizenDocumentReadStore documents, CitizenDocumentIssuanceStore issuance,
                                     CitizenDocumentManagementStore management, PersonalDataProtector protector,
                                     OperationalAuditStore audit, Clock clock) {
        this.access = access;
        this.citizens = citizens;
        this.documents = documents;
        this.issuance = issuance;
        this.management = management;
        this.protector = protector;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MyPassport passport(AccountId actorId, UUID citizenId) {
        access.requireActiveOperator(actorId);
        CitizenId id = requireCitizen(citizenId).id();
        return documents.findPassportByCitizenId(id).orElseThrow(DocumentNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public MyNationalIdentity nationalIdentity(AccountId actorId, UUID citizenId) {
        access.requireActiveOperator(actorId);
        CitizenId id = requireCitizen(citizenId).id();
        return documents.findNationalIdentityByCitizenId(id).orElseThrow(DocumentNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public MyBirthCertificate birthCertificate(AccountId actorId, UUID citizenId) {
        access.requireActiveOperator(actorId);
        CitizenId id = requireCitizen(citizenId).id();
        return documents.findBirthCertificateByCitizenId(id).orElseThrow(DocumentNotFoundException::new);
    }

    @Transactional
    public MyPassport createPassport(AccountId actorId, UUID citizenId, MyPassport document) {
        access.requireActiveOperator(actorId);
        CitizenId id = requireCitizen(citizenId).id();
        validate(document);
        if (documents.findPassportByCitizenId(id).isPresent()) throw new DocumentAlreadyExistsException();
        MyPassport saved = persist(() -> issuance.issuePassport(id, document, actorId, Instant.now(clock)));
        audit(actorId, citizenId, "PASSPORT_DOCUMENT_CREATED");
        return saved;
    }

    @Transactional
    public MyPassport updatePassport(AccountId actorId, UUID citizenId, MyPassport document) {
        access.requireActiveOperator(actorId);
        CitizenId id = requireCitizen(citizenId).id();
        validate(document);
        MyPassport saved = persist(() -> management.updatePassport(id, document));
        audit(actorId, citizenId, "PASSPORT_DOCUMENT_UPDATED");
        return saved;
    }

    @Transactional
    public MyNationalIdentity createNationalIdentity(AccountId actorId, UUID citizenId, MyNationalIdentity document) {
        access.requireActiveOperator(actorId);
        Citizen citizen = requireCitizen(citizenId);
        MyNationalIdentity verified = validateIdentity(citizen, document);
        if (documents.findNationalIdentityByCitizenId(citizen.id()).isPresent()) throw new DocumentAlreadyExistsException();
        MyNationalIdentity saved = persist(() -> issuance.issueNationalIdentity(citizen.id(), verified, actorId, Instant.now(clock)));
        audit(actorId, citizenId, "NATIONAL_IDENTITY_DOCUMENT_CREATED");
        return saved;
    }

    @Transactional
    public MyNationalIdentity updateNationalIdentity(AccountId actorId, UUID citizenId, MyNationalIdentity document) {
        access.requireActiveOperator(actorId);
        Citizen citizen = requireCitizen(citizenId);
        MyNationalIdentity verified = validateIdentity(citizen, document);
        MyNationalIdentity saved = persist(() -> management.updateNationalIdentity(citizen.id(), verified));
        audit(actorId, citizenId, "NATIONAL_IDENTITY_DOCUMENT_UPDATED");
        return saved;
    }

    @Transactional
    public MyBirthCertificate createBirthCertificate(AccountId actorId, UUID citizenId, MyBirthCertificate document) {
        access.requireActiveOperator(actorId);
        CitizenId id = requireCitizen(citizenId).id();
        validate(document);
        if (documents.findBirthCertificateByCitizenId(id).isPresent()) throw new DocumentAlreadyExistsException();
        MyBirthCertificate saved = persist(() -> issuance.issueBirthCertificate(id, document, actorId, Instant.now(clock)));
        audit(actorId, citizenId, "BIRTH_CERTIFICATE_DOCUMENT_CREATED");
        return saved;
    }

    @Transactional
    public MyBirthCertificate updateBirthCertificate(AccountId actorId, UUID citizenId, MyBirthCertificate document) {
        access.requireActiveOperator(actorId);
        CitizenId id = requireCitizen(citizenId).id();
        validate(document);
        MyBirthCertificate saved = persist(() -> management.updateBirthCertificate(id, document));
        audit(actorId, citizenId, "BIRTH_CERTIFICATE_DOCUMENT_UPDATED");
        return saved;
    }

    private Citizen requireCitizen(UUID citizenId) {
        return citizens.findById(new CitizenId(citizenId)).orElseThrow(() -> new NotFoundException("Citizen not found"));
    }

    private MyNationalIdentity validateIdentity(Citizen citizen, MyNationalIdentity value) {
        require(value, "National identity document is required");
        String canonicalNationalId = protector.decrypt(citizen.nationalId().ciphertext());
        if (!canonicalNationalId.equals(text(value.nationalId(), "National ID", 32))) {
            throw new IllegalArgumentException("National ID must match the citizen registry");
        }
        text(value.cardSerial(), "Card serial", 64);
        text(value.firstName(), "First name", 100);
        validateDates(value.dateOfBirth(), value.issuedOn(), value.expiresOn());
        return value;
    }

    private static void validate(MyPassport value) {
        require(value, "Passport document is required");
        text(value.passportNumber(), "Passport number", 64);
        text(value.firstName(), "First name", 100);
        validateDates(value.dateOfBirth(), value.issuedOn(), value.expiresOn());
    }

    private static void validate(MyBirthCertificate value) {
        require(value, "Birth certificate document is required");
        text(value.certificateNumber(), "Certificate number", 64);
        text(value.fullName(), "Full name", 200);
        if (value.birthDate() == null) throw new IllegalArgumentException("Birth date is required");
        if (value.birthDate().isAfter(LocalDate.now())) throw new IllegalArgumentException("Birth date cannot be in the future");
        if (value.declarationDate() != null && value.declarationDate().isBefore(value.birthDate())) {
            throw new IllegalArgumentException("Declaration date cannot be before birth date");
        }
    }

    private static void validateDates(LocalDate birthDate, LocalDate issuedOn, LocalDate expiresOn) {
        if (birthDate == null || issuedOn == null || expiresOn == null) {
            throw new IllegalArgumentException("Birth, issue and expiry dates are required");
        }
        if (birthDate.isAfter(issuedOn)) throw new IllegalArgumentException("Issue date cannot be before birth date");
        if (!expiresOn.isAfter(issuedOn)) throw new IllegalArgumentException("Expiry date must be after issue date");
    }

    private static <T> T require(T value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        return value;
    }

    private static String text(String value, String field, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new IllegalArgumentException(field + " is required and must not exceed " + max + " characters");
        }
        return value.trim();
    }

    private void audit(AccountId actorId, UUID citizenId, String action) {
        audit.record(actorId.value().toString(), action, "CITIZEN", citizenId.toString(), null, Instant.now(clock));
    }

    private static <T> T persist(Supplier<T> action) {
        try {
            return action.get();
        } catch (DataIntegrityViolationException exception) {
            throw new DocumentAlreadyExistsException();
        }
    }
}
