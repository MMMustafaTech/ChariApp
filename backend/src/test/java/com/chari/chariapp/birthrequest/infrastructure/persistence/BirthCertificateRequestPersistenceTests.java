package com.chari.chariapp.birthrequest.infrastructure.persistence;

import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStore;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequest;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequestKind;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BirthCertificateRequestPersistenceTests {

    @Autowired
    private CitizenStore citizens;

    @Autowired
    private BirthCertificateRequestStore requests;

    @Test
    void allowsOneOpenRequestForEachBirthRequestKind() {
        Instant now = Instant.parse("2026-09-18T00:00:00Z");
        String lookup = UUID.randomUUID().toString().replace("-", "").repeat(2);
        Citizen citizen = citizens.save(new Citizen(
                CitizenId.newId(),
                new NationalIdReference(lookup, "encrypted-national-id"),
                null,
                null,
                now
        ));

        requests.save(BirthCertificateRequest.submitted(
                citizen.id(), BirthCertificateRequestKind.CERTIFICATE_EXTRACT, null, now
        ));
        requests.save(BirthCertificateRequest.submitted(
                citizen.id(), BirthCertificateRequestKind.NEWBORN_REGISTRATION, null, now.plusSeconds(1)
        ));

        assertThat(requests.hasOpenRequest(citizen.id(), BirthCertificateRequestKind.CERTIFICATE_EXTRACT)).isTrue();
        assertThat(requests.hasOpenRequest(citizen.id(), BirthCertificateRequestKind.NEWBORN_REGISTRATION)).isTrue();
        assertThat(requests.findByCitizenId(citizen.id())).hasSize(2);
    }
}
