package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.appointment.application.port.out.AppointmentStore;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStore;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequest;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStore;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequest;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.operations.domain.UnifiedRequestStatus;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.request.domain.PassportRequest;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CitizenDirectoryService {
    private final PassportRequestActorAccess access;
    private final CitizenStore citizens;
    private final AccountStore accounts;
    private final CitizenDocumentReadStore documents;
    private final PassportRequestStore passports;
    private final NationalIdentityRequestStore identities;
    private final BirthCertificateRequestStore births;
    private final AppointmentStore appointments;
    private final PersonalDataProtector protector;
    private final OperationalAuditStore audit;
    private final Clock clock;

    public CitizenDirectoryService(PassportRequestActorAccess access, CitizenStore citizens, AccountStore accounts,
                                   CitizenDocumentReadStore documents, PassportRequestStore passports,
                                   NationalIdentityRequestStore identities, BirthCertificateRequestStore births,
                                   AppointmentStore appointments, PersonalDataProtector protector,
                                   OperationalAuditStore audit, Clock clock) {
        this.access = access;
        this.citizens = citizens;
        this.accounts = accounts;
        this.documents = documents;
        this.passports = passports;
        this.identities = identities;
        this.births = births;
        this.appointments = appointments;
        this.protector = protector;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public OperationsPage<CitizenSummaryView> list(AccountId actorId, String query, AccountStatus status,
                                                    int page, int size) {
        access.requireActiveOperator(actorId);
        validatePage(page, size);
        String normalized = query == null || query.isBlank() ? null : query.trim().toLowerCase(Locale.ROOT);
        List<CitizenSummaryView> filtered = citizens.findAll().stream()
                .map(this::summary)
                .filter(value -> status == null || value.accountStatus() == status)
                .filter(value -> matches(value, normalized))
                .sorted(Comparator.comparing(CitizenSummaryView::createdAt).reversed()
                        .thenComparing(value -> value.citizenId().toString()))
                .toList();
        int from = (int) Math.min((long) page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        int totalPages = filtered.isEmpty() ? 0 : (filtered.size() + size - 1) / size;
        return new OperationsPage<>(filtered.subList(from, to), page, size, filtered.size(), totalPages);
    }

    @Transactional(readOnly = true)
    public CitizenDetailsView details(AccountId actorId, UUID citizenId) {
        access.requireActiveOperator(actorId);
        Citizen citizen = requireCitizen(citizenId);
        CitizenId id = citizen.id();
        List<UnifiedServiceRequestView> requests = new ArrayList<>();
        passports.findByCitizenId(id).stream().map(this::request).forEach(requests::add);
        identities.findByCitizenId(id).stream().map(this::request).forEach(requests::add);
        births.findByCitizenId(id).stream().map(this::request).forEach(requests::add);
        requests.sort(Comparator.comparing(UnifiedServiceRequestView::submittedAt).reversed());
        return new CitizenDetailsView(summary(citizen),
                documents.findNationalIdentityByCitizenId(id).orElse(null),
                documents.findPassportByCitizenId(id).orElse(null),
                documents.findBirthCertificateByCitizenId(id).orElse(null),
                documents.findDependentBirthCertificatesByCitizenId(id),
                requests,
                appointments.findByCitizenId(id).stream()
                        .sorted(Comparator.comparing(com.chari.chariapp.appointment.domain.Appointment::startsAt).reversed())
                        .toList());
    }

    @Transactional
    public CitizenSummaryView updateStatus(AccountId actorId, UUID citizenId, AccountStatus status) {
        access.requireActiveOperator(actorId);
        if (status != AccountStatus.ACTIVE && status != AccountStatus.DISABLED) {
            throw new IllegalArgumentException("Citizen account status must be ACTIVE or DISABLED");
        }
        Citizen citizen = requireCitizen(citizenId);
        Account account = accounts.findByCitizenId(citizen.id())
                .orElseThrow(() -> new NotFoundException("Citizen account not found"));
        accounts.updateStatus(account.id(), status);
        audit.record(actorId.value().toString(), "CITIZEN_ACCOUNT_STATUS_CHANGED", "CITIZEN",
                citizenId.toString(), "status=" + status.name(), Instant.now(clock));
        return summary(citizen);
    }

    private CitizenSummaryView summary(Citizen citizen) {
        Account account = accounts.findByCitizenId(citizen.id()).orElse(null);
        MyNationalIdentity identity = documents.findNationalIdentityByCitizenId(citizen.id()).orElse(null);
        MyPassport passport = documents.findPassportByCitizenId(citizen.id()).orElse(null);
        MyBirthCertificate birth = documents.findBirthCertificateByCitizenId(citizen.id()).orElse(null);
        String name = identity != null ? join(identity.firstName(), identity.lastName())
                : passport != null ? join(passport.firstName(), passport.lastName())
                : birth != null ? birth.fullName() : null;
        return new CitizenSummaryView(
                citizen.id().value(),
                account == null ? null : account.id().value(),
                protector.decrypt(citizen.nationalId().ciphertext()),
                name,
                account == null ? null : protector.decrypt(account.email().ciphertext()),
                citizen.verifiedPhoneOptional().map(phone -> protector.decrypt(phone.ciphertext())).orElse(null),
                citizen.verifiedPhoneOptional().isPresent(),
                account == null ? null : account.status(),
                citizen.createdAt()
        );
    }

    private UnifiedServiceRequestView request(PassportRequest value) {
        return request(value.id(), ServiceRequestType.PASSPORT, value.kind().name(), value.status().name(),
                value.citizenId(), value.requestReason(), value.submissionDetails(), value.reviewedBy(), value.submittedAt(),
                value.reviewedAt(), value.decisionReason());
    }

    private UnifiedServiceRequestView request(NationalIdentityRequest value) {
        return request(value.id(), ServiceRequestType.NATIONAL_IDENTITY, value.kind().name(), value.status().name(),
                value.citizenId(), value.requestReason(), value.submissionDetails(), value.reviewedBy(), value.submittedAt(),
                value.reviewedAt(), value.decisionReason());
    }

    private UnifiedServiceRequestView request(BirthCertificateRequest value) {
        return request(value.id(), ServiceRequestType.BIRTH_CERTIFICATE, value.kind().name(), value.status().name(),
                value.citizenId(), value.requestReason(), null, value.reviewedBy(), value.submittedAt(),
                value.reviewedAt(), value.decisionReason());
    }

    private UnifiedServiceRequestView request(UUID id, ServiceRequestType type, String kind, String status,
                                              CitizenId citizenId, String reason,
                                              com.chari.chariapp.request.domain.ServiceRequestSubmissionDetails submissionDetails,
                                              AccountId reviewedBy,
                                              Instant submittedAt, Instant reviewedAt, String decisionReason) {
        return new UnifiedServiceRequestView(id, type, kind, UnifiedRequestStatus.valueOf(status),
                citizenId.value(), protector.decrypt(requireCitizen(citizenId.value()).nationalId().ciphertext()),
                reason, submissionDetails, reviewedBy == null ? null : reviewedBy.value(), submittedAt, reviewedAt, decisionReason);
    }

    private Citizen requireCitizen(UUID id) {
        return citizens.findById(new CitizenId(id)).orElseThrow(() -> new NotFoundException("Citizen not found"));
    }

    private static boolean matches(CitizenSummaryView value, String query) {
        if (query == null) return true;
        return contains(value.citizenId().toString(), query)
                || contains(value.nationalId(), query)
                || contains(value.fullName(), query)
                || contains(value.email(), query)
                || contains(value.phoneNumber(), query);
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private static String join(String first, String last) {
        String value = ((first == null ? "" : first.trim()) + " " + (last == null ? "" : last.trim())).trim();
        return value.isEmpty() ? null : value;
    }

    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be >= 0 and size must be 1-100");
        }
    }
}
