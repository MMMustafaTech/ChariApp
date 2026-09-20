package com.chari.chariapp.document.infrastructure.migration;

import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.infrastructure.persistence.SpringDataCitizenRepository;
import com.chari.chariapp.dto.BirthCertificateResponse;
import com.chari.chariapp.dto.NationalIdResponse;
import com.chari.chariapp.dto.PassportResponse;
import com.chari.chariapp.document.infrastructure.persistence.BirthCertificateDocumentJpaEntity;
import com.chari.chariapp.document.infrastructure.persistence.EncryptedDocumentPayloadCodec;
import com.chari.chariapp.document.infrastructure.persistence.NationalIdentityDocumentJpaEntity;
import com.chari.chariapp.document.infrastructure.persistence.PassportDocumentJpaEntity;
import com.chari.chariapp.document.infrastructure.persistence.SpringDataBirthCertificateDocumentRepository;
import com.chari.chariapp.document.infrastructure.persistence.SpringDataNationalIdentityDocumentRepository;
import com.chari.chariapp.document.infrastructure.persistence.SpringDataPassportDocumentRepository;
import com.chari.chariapp.repository.NormalizedCitizenDocumentRepository;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Idempotent migration from the normalized import model to the encrypted document source of truth. */
@Service
public class DocumentBackfillService {
    private final SpringDataCitizenRepository citizenRepository;
    private final NormalizedCitizenDocumentRepository normalizedDocuments;
    private final SpringDataPassportDocumentRepository passports;
    private final SpringDataNationalIdentityDocumentRepository nationalIdentities;
    private final SpringDataBirthCertificateDocumentRepository birthCertificates;
    private final EncryptedDocumentPayloadCodec payloadCodec;
    private final PersonalDataProtector dataProtector;

    public DocumentBackfillService(
            SpringDataCitizenRepository citizenRepository,
            NormalizedCitizenDocumentRepository normalizedDocuments,
            SpringDataPassportDocumentRepository passports,
            SpringDataNationalIdentityDocumentRepository nationalIdentities,
            SpringDataBirthCertificateDocumentRepository birthCertificates,
            EncryptedDocumentPayloadCodec payloadCodec,
            PersonalDataProtector dataProtector
    ) {
        this.citizenRepository = citizenRepository;
        this.normalizedDocuments = normalizedDocuments;
        this.passports = passports;
        this.nationalIdentities = nationalIdentities;
        this.birthCertificates = birthCertificates;
        this.payloadCodec = payloadCodec;
        this.dataProtector = dataProtector;
    }

    @Transactional
    public BackfillReport backfillAll() {
        int created = 0;
        int skipped = 0;
        int citizensVisited = 0;
        for (Citizen citizen : citizenRepository.findAll().stream().map(entity -> entity.toDomain()).toList()) {
            citizensVisited++;
            BackfillReport report = backfillCitizen(citizen);
            created += report.documentsCreated();
            skipped += report.documentsSkipped();
        }
        return new BackfillReport(citizensVisited, created, skipped);
    }

    @Transactional
    public BackfillReport backfillCitizen(Citizen citizen) {
        String nationalId = dataProtector.decrypt(citizen.nationalId().ciphertext());
        int created = 0;
        int skipped = 0;
        Instant now = Instant.now();

        var passport = normalizedDocuments.passport(nationalId);
        if (passport.isPresent()) {
            if (isBlank(passport.get().getPassportNumber())) skipped++; else if (copyPassport(citizen, passport.get(), now)) created++;
        }
        var identity = normalizedDocuments.nationalIdentity(nationalId);
        if (identity.isPresent()) {
            if (isBlank(identity.get().getNationalId())) skipped++; else if (copyNationalIdentity(citizen, identity.get(), now)) created++;
        }
        var certificate = normalizedDocuments.birthCertificate(nationalId);
        if (certificate.isPresent()) {
            if (isBlank(certificate.get().getCertificateNumber())) skipped++; else if (copyBirthCertificate(citizen, certificate.get(), now)) created++;
        }
        return new BackfillReport(1, created, skipped);
    }

    private boolean copyPassport(Citizen citizen, PassportResponse value, Instant now) {
        String lookup = dataProtector.lookup(value.getPassportNumber());
        if (passports.findFirstByCitizenIdOrderByRevisionDesc(citizen.id().value().toString()).isPresent()
                || passports.existsByDocumentNumberLookup(lookup)) return false;
        passports.save(new PassportDocumentJpaEntity(UUID.randomUUID().toString(), citizen.id(), lookup,
                payloadCodec.encrypt(passportPayload(value)), "ACTIVE", parseDate(value.getIssueDate()), parseDate(value.getExpiryDate()), 1, now));
        return true;
    }

    private boolean copyNationalIdentity(Citizen citizen, NationalIdResponse value, Instant now) {
        String lookup = dataProtector.lookup(value.getNationalId());
        if (nationalIdentities.findFirstByCitizenIdOrderByRevisionDesc(citizen.id().value().toString()).isPresent()
                || nationalIdentities.existsByDocumentNumberLookup(lookup)) return false;
        nationalIdentities.save(new NationalIdentityDocumentJpaEntity(UUID.randomUUID().toString(), citizen.id(), lookup,
                payloadCodec.encrypt(nationalIdentityPayload(value)), "ACTIVE", issueDate(value.getIssueDetails()), parseDate(value.getDateOfExpiry()), 1, now));
        return true;
    }

    private boolean copyBirthCertificate(Citizen citizen, BirthCertificateResponse value, Instant now) {
        String lookup = dataProtector.lookup(value.getCertificateNumber());
        if (birthCertificates.findFirstByCitizenIdOrderByRevisionDesc(citizen.id().value().toString()).isPresent()
                || birthCertificates.existsByDocumentNumberLookup(lookup)) return false;
        birthCertificates.save(new BirthCertificateDocumentJpaEntity(UUID.randomUUID().toString(), citizen.id(), lookup,
                payloadCodec.encrypt(birthCertificatePayload(value)), 1, now));
        return true;
    }

    private static Map<String, String> passportPayload(PassportResponse v) {
        return payload("passportNumber", v.getPassportNumber(), "firstName", v.getFirstName(), "lastName", v.getLastName(),
                "dateOfBirth", v.getBirthDate(), "placeOfBirth", v.getBirthPlace(), "issuedOn", v.getIssueDate(),
                "expiresOn", v.getExpiryDate(), "placeOfIssue", v.getIssuePlace(), "issuingAuthority", v.getIssueingAuthority(),
                "profession", v.getProfession(), "nationality", v.getNationality(), "sex", v.getGender());
    }

    private static Map<String, String> nationalIdentityPayload(NationalIdResponse v) {
        return payload("nationalId", v.getNationalId(), "firstName", v.getFirstName(), "lastName", v.getLastName(),
                "gender", v.getGender(), "placeOfBirth", v.getPlaceOfBirth(), "dateOfBirth", v.getDateofBirth(),
                "cardSerial", v.getCardSerial(), "placeOfIssue", issuePlace(v.getIssueDetails()), "issuedOn", date(issueDate(v.getIssueDetails())),
                "expiresOn", v.getDateOfExpiry(), "profession", v.getProfession(), "fatherName", v.getFatherName(),
                "motherName", v.getMotherName(), "address", v.getAddress(), "bloodGroup", v.getBloodGroup());
    }

    private static Map<String, String> birthCertificatePayload(BirthCertificateResponse v) {
        return payload("certificateNumber", v.getCertificateNumber(), "fullName", v.getFullName(), "gender", v.getGender(),
                "birthDate", v.getBirthDate(), "birthPlace", v.getBirthPlace(), "fatherName", v.getFatherName(),
                "fatherBirthDate", v.getFatherBirthDate(), "fatherBirthPlace", v.getFatherBirthPlace(),
                "fatherProfession", v.getFatherProfession(), "motherName", v.getMotherName(), "motherBirthDate", v.getMotherBirthDate(),
                "motherBirthPlace", v.getMotherBirthPlace(), "motherProfession", v.getMotherProfession(),
                "declarationDate", v.getDeclarationDate(), "address", v.getAddress());
    }

    private static Map<String, String> payload(String... entries) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) result.put(entries[index], entries[index + 1] == null ? "" : entries[index + 1]);
        return result;
    }

    private static String date(LocalDate value) { return value == null ? "" : value.toString(); }
    private static LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }
    private static LocalDate issueDate(String issueDetails) {
        if (issueDetails == null || issueDetails.isBlank()) return null;
        int separator = issueDetails.lastIndexOf('/');
        String candidate = separator < 0 ? issueDetails : issueDetails.substring(separator + 1);
        try {
            return LocalDate.parse(candidate);
        } catch (java.time.format.DateTimeParseException ignored) {
            return null;
        }
    }
    private static String issuePlace(String issueDetails) {
        if (issueDetails == null) return "";
        int separator = issueDetails.lastIndexOf('/');
        return separator < 0 ? issueDetails : issueDetails.substring(0, separator);
    }
    private static boolean isBlank(String value) { return value == null || value.isBlank(); }

    public record BackfillReport(int citizensVisited, int documentsCreated, int documentsSkipped) { }
}
