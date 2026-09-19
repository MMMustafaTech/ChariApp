package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.EmailReference;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStore;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStatusHistoryStore;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequest;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequestKind;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequestStatus;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStore;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStatusHistoryStore;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequest;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestKind;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestStatus;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.operations.domain.UnifiedRequestStatus;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.request.application.port.out.PassportRequestStatusHistoryStore;
import com.chari.chariapp.request.domain.PassportRequest;
import com.chari.chariapp.request.domain.PassportRequestKind;
import com.chari.chariapp.request.domain.PassportRequestStatus;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperationsRequestQueryServiceTests {
    private static final Instant NOW = Instant.parse("2026-09-19T08:00:00Z");

    @Test
    void combinesFiltersAndSummarizesAllServiceRequests() {
        PassportRequestActorAccess access = mock(PassportRequestActorAccess.class);
        PassportRequestStore passports = mock(PassportRequestStore.class);
        NationalIdentityRequestStore identities = mock(NationalIdentityRequestStore.class);
        BirthCertificateRequestStore births = mock(BirthCertificateRequestStore.class);
        PassportRequestStatusHistoryStore passportHistory = mock(PassportRequestStatusHistoryStore.class);
        NationalIdentityRequestStatusHistoryStore identityHistory = mock(NationalIdentityRequestStatusHistoryStore.class);
        BirthCertificateRequestStatusHistoryStore birthHistory = mock(BirthCertificateRequestStatusHistoryStore.class);
        CitizenStore citizens = mock(CitizenStore.class);
        PersonalDataProtector protector = mock(PersonalDataProtector.class);
        AccountId operatorId = AccountId.newId();
        when(access.requireActiveOperator(operatorId)).thenReturn(operator(operatorId));
        when(passports.findByStatus(any())).thenReturn(List.of());
        when(identities.findByStatus(any())).thenReturn(List.of());
        when(births.findByStatus(any())).thenReturn(List.of());

        Citizen passportCitizen = citizen("CID002");
        Citizen identityCitizen = citizen("CID003");
        Citizen birthCitizen = citizen("CID004");
        when(citizens.findById(passportCitizen.id())).thenReturn(Optional.of(passportCitizen));
        when(citizens.findById(identityCitizen.id())).thenReturn(Optional.of(identityCitizen));
        when(citizens.findById(birthCitizen.id())).thenReturn(Optional.of(birthCitizen));
        when(protector.decrypt("cipher-CID002")).thenReturn("CID002");
        when(protector.decrypt("cipher-CID003")).thenReturn("CID003");
        when(protector.decrypt("cipher-CID004")).thenReturn("CID004");

        PassportRequest passport = PassportRequest.submitted(passportCitizen.id(), PassportRequestKind.ISSUANCE, null, NOW.minusSeconds(60));
        NationalIdentityRequest identity = NationalIdentityRequest.submitted(identityCitizen.id(), NationalIdentityRequestKind.ISSUANCE, null, NOW.minusSeconds(30));
        BirthCertificateRequest birth = BirthCertificateRequest.submitted(birthCitizen.id(), BirthCertificateRequestKind.CERTIFICATE_EXTRACT, null, NOW);
        when(passports.findByStatus(PassportRequestStatus.SUBMITTED)).thenReturn(List.of(passport));
        when(identities.findByStatus(NationalIdentityRequestStatus.SUBMITTED)).thenReturn(List.of(identity));
        when(births.findByStatus(BirthCertificateRequestStatus.SUBMITTED)).thenReturn(List.of(birth));

        OperationsRequestQueryService service = new OperationsRequestQueryService(access, passports, identities,
                births, passportHistory, identityHistory, birthHistory, citizens, protector,
                Clock.fixed(NOW, ZoneOffset.UTC));

        OperationsDashboard dashboard = service.dashboard(operatorId);
        assertThat(dashboard.totalRequests()).isEqualTo(3);
        assertThat(dashboard.submitted()).isEqualTo(3);
        assertThat(dashboard.submittedToday()).isEqualTo(3);
        assertThat(dashboard.requestsByService()).containsEntry(ServiceRequestType.PASSPORT, 1L)
                .containsEntry(ServiceRequestType.NATIONAL_IDENTITY, 1L)
                .containsEntry(ServiceRequestType.BIRTH_CERTIFICATE, 1L);
        assertThat(dashboard.recentRequests()).extracting(UnifiedServiceRequestView::citizenNationalId)
                .containsExactly("CID004", "CID003", "CID002");

        OperationsPage<UnifiedServiceRequestView> filtered = service.list(operatorId, ServiceRequestType.PASSPORT,
                UnifiedRequestStatus.SUBMITTED, "cid002", null, null, 0, 20);
        assertThat(filtered.totalElements()).isEqualTo(1);
        assertThat(filtered.content()).singleElement().extracting(UnifiedServiceRequestView::id)
                .isEqualTo(passport.id());

        OperationsRequestReport report = service.report(operatorId, NOW.minusSeconds(120), NOW.plusSeconds(1));
        assertThat(report.totalRequests()).isEqualTo(3);
        assertThat(report.requestsByService()).containsEntry(ServiceRequestType.PASSPORT, 1L)
                .containsEntry(ServiceRequestType.NATIONAL_IDENTITY, 1L)
                .containsEntry(ServiceRequestType.BIRTH_CERTIFICATE, 1L);
        assertThat(report.submissionsByDay()).containsEntry(java.time.LocalDate.of(2026, 9, 19), 3L);
        assertThat(report.averageReviewHours()).isZero();
    }

    private static Citizen citizen(String nationalId) {
        return new Citizen(CitizenId.newId(), new NationalIdReference("a".repeat(64), "cipher-" + nationalId),
                null, null, NOW.minusSeconds(3600));
    }

    private static Account operator(AccountId id) {
        return new Account(id, null, new EmailReference("b".repeat(64), "cipher-email"), "hash",
                AccountStatus.ACTIVE, Set.of(AccountRole.EMPLOYEE), NOW.minusSeconds(3600));
    }
}
