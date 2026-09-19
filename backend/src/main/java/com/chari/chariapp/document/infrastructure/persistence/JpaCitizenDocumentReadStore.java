package com.chari.chariapp.document.infrastructure.persistence;

import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.document.domain.DependentBirthCertificate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@Repository
public class JpaCitizenDocumentReadStore implements CitizenDocumentReadStore {
    private final SpringDataPassportDocumentRepository passportDocuments;
    private final SpringDataNationalIdentityDocumentRepository nationalIdentityDocuments;
    private final SpringDataBirthCertificateDocumentRepository birthCertificateDocuments;
    private final SpringDataDependentBirthCertificateDocumentRepository dependentBirthCertificates;
    private final EncryptedDocumentPayloadCodec payloadCodec;

    public JpaCitizenDocumentReadStore(
            SpringDataPassportDocumentRepository passportDocuments,
            SpringDataNationalIdentityDocumentRepository nationalIdentityDocuments,
            SpringDataBirthCertificateDocumentRepository birthCertificateDocuments,
            SpringDataDependentBirthCertificateDocumentRepository dependentBirthCertificates,
            EncryptedDocumentPayloadCodec payloadCodec
    ) {
        this.passportDocuments = passportDocuments;
        this.nationalIdentityDocuments = nationalIdentityDocuments;
        this.birthCertificateDocuments = birthCertificateDocuments;
        this.dependentBirthCertificates = dependentBirthCertificates;
        this.payloadCodec = payloadCodec;
    }

    @Override
    public Optional<MyPassport> findPassportByCitizenId(CitizenId citizenId) {
        return passportDocuments.findFirstByCitizenIdOrderByRevisionDesc(citizenId.value().toString())
                .map(value -> passport(payloadCodec.decrypt(value.encryptedPayload())));
    }

    @Override
    public Optional<MyNationalIdentity> findNationalIdentityByCitizenId(CitizenId citizenId) {
        return nationalIdentityDocuments.findFirstByCitizenIdOrderByRevisionDesc(citizenId.value().toString())
                .map(value -> nationalIdentity(payloadCodec.decrypt(value.encryptedPayload())));
    }

    @Override
    public Optional<MyBirthCertificate> findBirthCertificateByCitizenId(CitizenId citizenId) {
        return birthCertificateDocuments.findFirstByCitizenIdOrderByRevisionDesc(citizenId.value().toString())
                .map(value -> birthCertificate(payloadCodec.decrypt(value.encryptedPayload())));
    }

    @Override
    public List<DependentBirthCertificate> findDependentBirthCertificatesByCitizenId(CitizenId citizenId) {
        return dependentBirthCertificates.findByParentCitizenIdOrderByCreatedAtDesc(citizenId.value().toString())
                .stream()
                .map(value -> new DependentBirthCertificate(value.id(), value.requestId(), value.parentCitizenId(),
                        birthCertificate(payloadCodec.decrypt(value.encryptedPayload())), value.createdAt()))
                .toList();
    }

    @Override
    public Optional<DependentBirthCertificate> findDependentBirthCertificateById(java.util.UUID id) {
        return dependentBirthCertificates.findById(id.toString())
                .map(value -> new DependentBirthCertificate(value.id(), value.requestId(), value.parentCitizenId(),
                        birthCertificate(payloadCodec.decrypt(value.encryptedPayload())), value.createdAt()));
    }

    private MyPassport passport(Map<String, String> p) {
        return new MyPassport(p.get("passportNumber"), p.get("firstName"), p.get("lastName"), date(p, "dateOfBirth"),
                p.get("placeOfBirth"), date(p, "issuedOn"), date(p, "expiresOn"), p.get("placeOfIssue"),
                p.get("issuingAuthority"), p.get("profession"), p.get("nationality"), p.get("sex"));
    }

    private MyNationalIdentity nationalIdentity(Map<String, String> p) {
        return new MyNationalIdentity(p.get("nationalId"), p.get("firstName"), p.get("lastName"), p.get("gender"),
                p.get("placeOfBirth"), date(p, "dateOfBirth"), p.get("cardSerial"), p.get("placeOfIssue"),
                date(p, "issuedOn"), date(p, "expiresOn"), p.get("profession"), p.get("fatherName"),
                p.get("motherName"), p.get("address"), p.get("bloodGroup"));
    }

    private MyBirthCertificate birthCertificate(Map<String, String> p) {
        return new MyBirthCertificate(p.get("certificateNumber"), p.get("fullName"), p.get("gender"), date(p, "birthDate"),
                p.get("birthPlace"), p.get("fatherName"), date(p, "fatherBirthDate"), p.get("fatherBirthPlace"),
                p.get("fatherProfession"), p.get("motherName"), date(p, "motherBirthDate"), p.get("motherBirthPlace"),
                p.get("motherProfession"), date(p, "declarationDate"), p.get("address"));
    }

    private LocalDate date(Map<String, String> payload, String field) {
        String value = payload.get(field);
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }
}
