package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import com.chari.chariapp.document.application.port.out.CitizenDocumentIssuanceStore;
import com.chari.chariapp.document.application.port.out.CitizenDocumentManagementStore;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationsDocumentServiceTests {
    private static final Instant NOW = Instant.parse("2026-09-20T08:00:00Z");

    @Test
    void createsPassportAndAuditsTheOperatorAction() {
        Fixture fixture = fixture();
        MyPassport passport = passport();
        when(fixture.documents.findPassportByCitizenId(fixture.citizen.id())).thenReturn(Optional.empty());
        when(fixture.issuance.issuePassport(fixture.citizen.id(), passport, fixture.actor, NOW)).thenReturn(passport);

        MyPassport result = fixture.service.createPassport(fixture.actor, fixture.citizen.id().value(), passport);

        assertThat(result).isEqualTo(passport);
        verify(fixture.audit).record(fixture.actor.value().toString(), "PASSPORT_DOCUMENT_CREATED", "CITIZEN",
                fixture.citizen.id().value().toString(), null, NOW);
    }

    @Test
    void rejectsIdentityWhoseNationalIdDoesNotMatchCitizenRegistry() {
        Fixture fixture = fixture();
        when(fixture.protector.decrypt("national-id-cipher")).thenReturn("CID002");
        MyNationalIdentity identity = new MyNationalIdentity("CID999", "Ahmed", "Ali", "Male", "Bolu",
                LocalDate.parse("1995-03-12"), "AA123", "Bolu", LocalDate.parse("2020-01-01"),
                LocalDate.parse("2030-01-01"), "Engineer", "Omar", "Fatma", "Address", "A+");

        assertThatThrownBy(() -> fixture.service.createNationalIdentity(
                fixture.actor, fixture.citizen.id().value(), identity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("match the citizen registry");
    }

    private static Fixture fixture() {
        AccountId actor = AccountId.newId();
        Citizen citizen = new Citizen(CitizenId.newId(),
                new NationalIdReference("a".repeat(64), "national-id-cipher"), null, null, NOW);
        PassportRequestActorAccess access = mock(PassportRequestActorAccess.class);
        CitizenStore citizens = mock(CitizenStore.class);
        CitizenDocumentReadStore documents = mock(CitizenDocumentReadStore.class);
        CitizenDocumentIssuanceStore issuance = mock(CitizenDocumentIssuanceStore.class);
        CitizenDocumentManagementStore management = mock(CitizenDocumentManagementStore.class);
        PersonalDataProtector protector = mock(PersonalDataProtector.class);
        OperationalAuditStore audit = mock(OperationalAuditStore.class);
        when(citizens.findById(citizen.id())).thenReturn(Optional.of(citizen));
        OperationsDocumentService service = new OperationsDocumentService(access, citizens, documents, issuance,
                management, protector, audit, Clock.fixed(NOW, ZoneOffset.UTC));
        return new Fixture(actor, citizen, documents, issuance, protector, audit, service);
    }

    private static MyPassport passport() {
        return new MyPassport("P00001", "Ahmed", "Ali", LocalDate.parse("1995-03-12"), "Bolu",
                LocalDate.parse("2020-01-01"), LocalDate.parse("2030-01-01"), "Bolu", "Authority",
                "Engineer", "Chadian", "Male");
    }

    private record Fixture(AccountId actor, Citizen citizen, CitizenDocumentReadStore documents,
                           CitizenDocumentIssuanceStore issuance, PersonalDataProtector protector,
                           OperationalAuditStore audit, OperationsDocumentService service) {
    }
}
