package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.domain.*;
import com.chari.chariapp.appointment.application.port.out.AppointmentStore;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStore;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStore;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CitizenDirectoryServiceTests {
    private static final Instant NOW = Instant.parse("2026-09-19T11:00:00Z");
    private static final String LOOKUP = "a".repeat(64);

    @Test
    void searchesCitizenDirectoryByDocumentName() {
        Fixture fixture = fixture();
        MyNationalIdentity identity = new MyNationalIdentity(
                "CID002", "Mohammed", "Ali", "Male", "N'Djamena",
                LocalDate.parse("1995-03-12"), "AA123", "N'Djamena",
                LocalDate.parse("2020-01-01"), LocalDate.parse("2030-01-01"),
                "Engineer", "Ali", "Fatima", "Address", "A+"
        );
        when(fixture.documents.findNationalIdentityByCitizenId(fixture.citizen.id()))
                .thenReturn(Optional.of(identity));

        OperationsPage<CitizenSummaryView> page = fixture.service.list(
                fixture.employee.id(), "mohammed", AccountStatus.ACTIVE, 0, 20
        );

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content()).singleElement().satisfies(result -> {
            assertThat(result.nationalId()).isEqualTo("CID002");
            assertThat(result.fullName()).isEqualTo("Mohammed Ali");
            assertThat(result.email()).isEqualTo("citizen@example.com");
        });
    }

    @Test
    void disablesCitizenAccountAndAuditsTheChange() {
        Fixture fixture = fixture();

        CitizenSummaryView updated = fixture.service.updateStatus(
                fixture.employee.id(), fixture.citizen.id().value(), AccountStatus.DISABLED
        );

        verify(fixture.accounts).updateStatus(fixture.citizenAccount.id(), AccountStatus.DISABLED);
        verify(fixture.audit).record(
                fixture.employee.id().value().toString(),
                "CITIZEN_ACCOUNT_STATUS_CHANGED",
                "CITIZEN",
                fixture.citizen.id().value().toString(),
                "status=DISABLED",
                NOW
        );
        assertThat(updated.citizenId()).isEqualTo(fixture.citizen.id().value());
    }

    private static Fixture fixture() {
        AccountId employeeId = AccountId.newId();
        CitizenId citizenId = CitizenId.newId();
        Account employee = new Account(employeeId, null,
                new EmailReference(LOOKUP, "employee-email"), "hash", AccountStatus.ACTIVE,
                Set.of(AccountRole.EMPLOYEE), Set.of(StaffPermission.CITIZEN_VIEW, StaffPermission.CITIZEN_EDIT), NOW);
        Account citizenAccount = new Account(AccountId.newId(), citizenId,
                new EmailReference(LOOKUP, "citizen-email"), "hash", AccountStatus.ACTIVE,
                Set.of(AccountRole.CITIZEN), NOW);
        Citizen citizen = new Citizen(citizenId, new NationalIdReference(LOOKUP, "national-id"),
                null, null, NOW.minusSeconds(60));

        AccountStore accounts = mock(AccountStore.class);
        CitizenStore citizens = mock(CitizenStore.class);
        CitizenDocumentReadStore documents = mock(CitizenDocumentReadStore.class);
        PassportRequestStore passports = mock(PassportRequestStore.class);
        NationalIdentityRequestStore identities = mock(NationalIdentityRequestStore.class);
        BirthCertificateRequestStore births = mock(BirthCertificateRequestStore.class);
        AppointmentStore appointments = mock(AppointmentStore.class);
        PersonalDataProtector protector = mock(PersonalDataProtector.class);
        OperationalAuditStore audit = mock(OperationalAuditStore.class);

        when(accounts.findById(employeeId)).thenReturn(Optional.of(employee));
        when(accounts.findByCitizenId(citizenId)).thenReturn(Optional.of(citizenAccount));
        when(accounts.updateStatus(any(), any())).thenReturn(citizenAccount);
        when(citizens.findAll()).thenReturn(List.of(citizen));
        when(citizens.findById(citizenId)).thenReturn(Optional.of(citizen));
        when(passports.findByCitizenId(citizenId)).thenReturn(List.of());
        when(identities.findByCitizenId(citizenId)).thenReturn(List.of());
        when(births.findByCitizenId(citizenId)).thenReturn(List.of());
        when(appointments.findByCitizenId(citizenId)).thenReturn(List.of());
        when(protector.decrypt("national-id")).thenReturn("CID002");
        when(protector.decrypt("citizen-email")).thenReturn("citizen@example.com");

        CitizenDirectoryService service = new CitizenDirectoryService(
                new PassportRequestActorAccess(accounts), citizens, accounts, documents,
                passports, identities, births, appointments, protector, audit,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        return new Fixture(service, employee, citizen, citizenAccount, accounts, documents, audit);
    }

    private record Fixture(CitizenDirectoryService service, Account employee, Citizen citizen,
                           Account citizenAccount, AccountStore accounts,
                           CitizenDocumentReadStore documents, OperationalAuditStore audit) {
    }
}
