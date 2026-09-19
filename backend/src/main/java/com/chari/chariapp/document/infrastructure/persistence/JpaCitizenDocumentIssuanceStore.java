package com.chari.chariapp.document.infrastructure.persistence;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.application.port.out.CitizenDocumentIssuanceStore;
import com.chari.chariapp.document.domain.DependentBirthCertificate;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class JpaCitizenDocumentIssuanceStore implements CitizenDocumentIssuanceStore {
    private final SpringDataPassportDocumentRepository passports;
    private final SpringDataNationalIdentityDocumentRepository identities;
    private final SpringDataBirthCertificateDocumentRepository births;
    private final SpringDataDependentBirthCertificateDocumentRepository dependents;
    private final EncryptedDocumentPayloadCodec codec;
    private final PersonalDataProtector protector;

    public JpaCitizenDocumentIssuanceStore(SpringDataPassportDocumentRepository passports,
                                           SpringDataNationalIdentityDocumentRepository identities,
                                           SpringDataBirthCertificateDocumentRepository births,
                                           SpringDataDependentBirthCertificateDocumentRepository dependents,
                                           EncryptedDocumentPayloadCodec codec,
                                           PersonalDataProtector protector) {
        this.passports = passports;
        this.identities = identities;
        this.births = births;
        this.dependents = dependents;
        this.codec = codec;
        this.protector = protector;
    }

    @Override
    public MyPassport issuePassport(CitizenId citizenId, MyPassport document, AccountId actorId, Instant now) {
        int revision = passports.findFirstByCitizenIdOrderByRevisionDesc(citizenId.value().toString())
                .map(value -> value.revision() + 1).orElse(1);
        passports.save(new PassportDocumentJpaEntity(UUID.randomUUID().toString(), citizenId,
                protector.lookup(document.passportNumber()), codec.encrypt(passport(document)), "ACTIVE",
                document.issuedOn(), document.expiresOn(), revision, now, actorId));
        return document;
    }

    @Override
    public MyNationalIdentity issueNationalIdentity(CitizenId citizenId, MyNationalIdentity document,
                                                     AccountId actorId, Instant now) {
        int revision = identities.findFirstByCitizenIdOrderByRevisionDesc(citizenId.value().toString())
                .map(value -> value.revision() + 1).orElse(1);
        identities.save(new NationalIdentityDocumentJpaEntity(UUID.randomUUID().toString(), citizenId,
                protector.lookup(document.cardSerial()), codec.encrypt(identity(document)), "ACTIVE",
                document.issuedOn(), document.expiresOn(), revision, now, actorId));
        return document;
    }

    @Override
    public MyBirthCertificate issueBirthCertificate(CitizenId citizenId, MyBirthCertificate document,
                                                     AccountId actorId, Instant now) {
        int revision = births.findFirstByCitizenIdOrderByRevisionDesc(citizenId.value().toString())
                .map(value -> value.revision() + 1).orElse(1);
        births.save(new BirthCertificateDocumentJpaEntity(UUID.randomUUID().toString(), citizenId,
                protector.lookup(document.certificateNumber()), codec.encrypt(birth(document)), revision, now, actorId));

        return document;
    }

    @Override
    public DependentBirthCertificate issueDependentBirthCertificate(CitizenId parentCitizenId, UUID requestId,
                                                                     MyBirthCertificate document, AccountId actorId,
                                                                     Instant now) {
        UUID id = UUID.randomUUID();
        dependents.save(new DependentBirthCertificateDocumentJpaEntity(id, requestId, parentCitizenId,
                protector.lookup(document.certificateNumber()), codec.encrypt(birth(document)), actorId, now));
        return new DependentBirthCertificate(id, requestId, parentCitizenId, document, now);
    }

    static Map<String, String> passport(MyPassport value) {
        Map<String, String> result = new LinkedHashMap<>();
        put(result, "passportNumber", value.passportNumber());
        put(result, "firstName", value.firstName());
        put(result, "lastName", value.lastName());
        put(result, "dateOfBirth", value.dateOfBirth());
        put(result, "placeOfBirth", value.placeOfBirth());
        put(result, "issuedOn", value.issuedOn());
        put(result, "expiresOn", value.expiresOn());
        put(result, "placeOfIssue", value.placeOfIssue());
        put(result, "issuingAuthority", value.issuingAuthority());
        put(result, "profession", value.profession());
        put(result, "nationality", value.nationality());
        put(result, "sex", value.sex());
        return result;
    }

    static Map<String, String> identity(MyNationalIdentity value) {
        Map<String, String> result = new LinkedHashMap<>();
        put(result, "nationalId", value.nationalId());
        put(result, "firstName", value.firstName());
        put(result, "lastName", value.lastName());
        put(result, "gender", value.gender());
        put(result, "placeOfBirth", value.placeOfBirth());
        put(result, "dateOfBirth", value.dateOfBirth());
        put(result, "cardSerial", value.cardSerial());
        put(result, "placeOfIssue", value.placeOfIssue());
        put(result, "issuedOn", value.issuedOn());
        put(result, "expiresOn", value.expiresOn());
        put(result, "profession", value.profession());
        put(result, "fatherName", value.fatherName());
        put(result, "motherName", value.motherName());
        put(result, "address", value.address());
        put(result, "bloodGroup", value.bloodGroup());
        return result;
    }

    static Map<String, String> birth(MyBirthCertificate value) {
        Map<String, String> result = new LinkedHashMap<>();
        put(result, "certificateNumber", value.certificateNumber());
        put(result, "fullName", value.fullName());
        put(result, "gender", value.gender());
        put(result, "birthDate", value.birthDate());
        put(result, "birthPlace", value.birthPlace());
        put(result, "fatherName", value.fatherName());
        put(result, "fatherBirthDate", value.fatherBirthDate());
        put(result, "fatherBirthPlace", value.fatherBirthPlace());
        put(result, "fatherProfession", value.fatherProfession());
        put(result, "motherName", value.motherName());
        put(result, "motherBirthDate", value.motherBirthDate());
        put(result, "motherBirthPlace", value.motherBirthPlace());
        put(result, "motherProfession", value.motherProfession());
        put(result, "declarationDate", value.declarationDate());
        put(result, "address", value.address());
        return result;
    }

    private static void put(Map<String, String> target, String key, Object value) {
        target.put(key, value == null ? null : value instanceof LocalDate date ? date.toString() : value.toString());
    }
}
