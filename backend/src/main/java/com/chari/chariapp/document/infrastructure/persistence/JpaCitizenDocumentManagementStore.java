package com.chari.chariapp.document.infrastructure.persistence;

import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.application.DocumentNotFoundException;
import com.chari.chariapp.document.application.port.out.CitizenDocumentManagementStore;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCitizenDocumentManagementStore implements CitizenDocumentManagementStore {
    private final SpringDataPassportDocumentRepository passports;
    private final SpringDataNationalIdentityDocumentRepository identities;
    private final SpringDataBirthCertificateDocumentRepository births;
    private final EncryptedDocumentPayloadCodec codec;
    private final PersonalDataProtector protector;

    public JpaCitizenDocumentManagementStore(SpringDataPassportDocumentRepository passports,
                                             SpringDataNationalIdentityDocumentRepository identities,
                                             SpringDataBirthCertificateDocumentRepository births,
                                             EncryptedDocumentPayloadCodec codec,
                                             PersonalDataProtector protector) {
        this.passports = passports;
        this.identities = identities;
        this.births = births;
        this.codec = codec;
        this.protector = protector;
    }

    @Override
    public MyPassport updatePassport(CitizenId citizenId, MyPassport document) {
        PassportDocumentJpaEntity entity = passports.findFirstByCitizenIdOrderByRevisionDesc(citizenId.value().toString())
                .orElseThrow(DocumentNotFoundException::new);
        entity.update(protector.lookup(document.passportNumber()),
                codec.encrypt(JpaCitizenDocumentIssuanceStore.passport(document)), document.issuedOn(), document.expiresOn());
        passports.save(entity);
        return document;
    }

    @Override
    public MyNationalIdentity updateNationalIdentity(CitizenId citizenId, MyNationalIdentity document) {
        NationalIdentityDocumentJpaEntity entity = identities.findFirstByCitizenIdOrderByRevisionDesc(citizenId.value().toString())
                .orElseThrow(DocumentNotFoundException::new);
        entity.update(protector.lookup(document.cardSerial()),
                codec.encrypt(JpaCitizenDocumentIssuanceStore.identity(document)), document.issuedOn(), document.expiresOn());
        identities.save(entity);
        return document;
    }

    @Override
    public MyBirthCertificate updateBirthCertificate(CitizenId citizenId, MyBirthCertificate document) {
        BirthCertificateDocumentJpaEntity entity = births.findFirstByCitizenIdOrderByRevisionDesc(citizenId.value().toString())
                .orElseThrow(DocumentNotFoundException::new);
        entity.update(protector.lookup(document.certificateNumber()),
                codec.encrypt(JpaCitizenDocumentIssuanceStore.birth(document)));
        births.save(entity);
        return document;
    }
}
