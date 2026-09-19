package com.chari.chariapp.document.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.birthrequest.application.port.out.NewbornRegistrationDetailsStore;
import com.chari.chariapp.birthrequest.domain.*;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.application.port.out.CitizenDocumentIssuanceStore;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.document.domain.DependentBirthCertificate;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.request.domain.PassportRequest;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentIssuanceServiceTests {
    private static final Instant NOW = Instant.parse("2026-09-19T12:00:00Z");

    @Test
    void issuesPassportFromNationalIdentityData() {
        Fixture fixture = fixture();
        CitizenId citizenId = CitizenId.newId();
        PassportRequest request = PassportRequest.submitted(citizenId, NOW.minusSeconds(60));
        MyNationalIdentity identity = new MyNationalIdentity(
                "CID002", "Mohammed", "Ali", "Male", "N'Djamena",
                LocalDate.parse("1995-03-12"), "AA123", "N'Djamena",
                LocalDate.parse("2020-01-01"), LocalDate.parse("2030-01-01"),
                "Engineer", "Ali", "Fatima", "Address", "A+"
        );
        when(fixture.documents.findNationalIdentityByCitizenId(citizenId)).thenReturn(Optional.of(identity));
        when(fixture.issuance.issuePassport(eq(citizenId), any(), eq(fixture.actor), eq(NOW)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        MyPassport issued = fixture.service.issue(request, fixture.actor, NOW);

        assertThat(issued.passportNumber()).startsWith("P2026");
        assertThat(issued.firstName()).isEqualTo("Mohammed");
        assertThat(issued.expiresOn()).isEqualTo(LocalDate.parse("2036-09-19"));
        verify(fixture.audit).record(fixture.actor.value().toString(), "PASSPORT_DOCUMENT_ISSUED",
                "SERVICE_REQUEST", request.id().toString(), null, NOW);
    }

    @Test
    void storesNewbornCertificateAsDependentDocument() {
        Fixture fixture = fixture();
        CitizenId parentId = CitizenId.newId();
        BirthCertificateRequest request = BirthCertificateRequest.submitted(
                parentId, BirthCertificateRequestKind.NEWBORN_REGISTRATION, null, NOW.minusSeconds(60)
        );
        NewbornRegistrationDetails details = new NewbornRegistrationDetails(
                "Ahmed", "Ali", LocalDate.parse("2026-08-20"), "Bolu", NewbornGender.MALE,
                "Omar Ali", "123456789", "Fatma Ali", "987654321"
        );
        when(fixture.newbornDetails.findByRequestId(request.id())).thenReturn(Optional.of(details));
        when(fixture.issuance.issueDependentBirthCertificate(eq(parentId), eq(request.id()), any(),
                eq(fixture.actor), eq(NOW))).thenAnswer(invocation -> new DependentBirthCertificate(
                UUID.randomUUID(), request.id(), parentId, invocation.getArgument(2), NOW
        ));

        MyBirthCertificate issued = fixture.service.issue(request, fixture.actor, NOW);

        assertThat(issued.fullName()).isEqualTo("Ahmed Ali");
        assertThat(issued.certificateNumber()).startsWith("BC2026");
        verify(fixture.issuance).issueDependentBirthCertificate(eq(parentId), eq(request.id()), any(),
                eq(fixture.actor), eq(NOW));
    }

    private static Fixture fixture() {
        CitizenDocumentReadStore documents = mock(CitizenDocumentReadStore.class);
        CitizenDocumentIssuanceStore issuance = mock(CitizenDocumentIssuanceStore.class);
        CitizenStore citizens = mock(CitizenStore.class);
        NewbornRegistrationDetailsStore newborn = mock(NewbornRegistrationDetailsStore.class);
        PersonalDataProtector protector = mock(PersonalDataProtector.class);
        OperationalAuditStore audit = mock(OperationalAuditStore.class);
        DocumentIssuanceService service = new DocumentIssuanceService(
                documents, issuance, citizens, newborn, protector, audit
        );
        return new Fixture(service, documents, issuance, newborn, audit, AccountId.newId());
    }

    private record Fixture(DocumentIssuanceService service, CitizenDocumentReadStore documents,
                           CitizenDocumentIssuanceStore issuance,
                           NewbornRegistrationDetailsStore newbornDetails,
                           OperationalAuditStore audit, AccountId actor) {
    }
}
