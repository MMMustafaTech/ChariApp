package com.chari.chariapp.operations.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStore;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStatusHistoryStore;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequest;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequestStatus;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStore;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStatusHistoryStore;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequest;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestStatus;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.operations.domain.UnifiedRequestStatus;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.request.application.port.out.PassportRequestStatusHistoryStore;
import com.chari.chariapp.request.domain.PassportRequest;
import com.chari.chariapp.request.domain.PassportRequestStatus;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class OperationsRequestQueryService {
    private static final Comparator<UnifiedServiceRequestView> NEWEST_FIRST =
            Comparator.comparing(UnifiedServiceRequestView::submittedAt).reversed()
                    .thenComparing(view -> view.id().toString());

    private final PassportRequestActorAccess access;
    private final PassportRequestStore passports;
    private final NationalIdentityRequestStore identities;
    private final BirthCertificateRequestStore birthCertificates;
    private final PassportRequestStatusHistoryStore passportHistory;
    private final NationalIdentityRequestStatusHistoryStore identityHistory;
    private final BirthCertificateRequestStatusHistoryStore birthHistory;
    private final CitizenStore citizens;
    private final PersonalDataProtector protector;
    private final Clock clock;

    public OperationsRequestQueryService(PassportRequestActorAccess access, PassportRequestStore passports,
                                         NationalIdentityRequestStore identities,
                                         BirthCertificateRequestStore birthCertificates,
                                         PassportRequestStatusHistoryStore passportHistory,
                                         NationalIdentityRequestStatusHistoryStore identityHistory,
                                         BirthCertificateRequestStatusHistoryStore birthHistory,
                                         CitizenStore citizens,
                                         PersonalDataProtector protector, Clock clock) {
        this.access = access; this.passports = passports; this.identities = identities;
        this.birthCertificates = birthCertificates; this.citizens = citizens;
        this.passportHistory = passportHistory; this.identityHistory = identityHistory; this.birthHistory = birthHistory;
        this.protector = protector; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public OperationsPage<UnifiedServiceRequestView> list(AccountId actorId, ServiceRequestType serviceType,
                                                           UnifiedRequestStatus status, String query,
                                                           Instant submittedFrom, Instant submittedTo,
                                                           int page, int size) {
        access.requireActiveOperator(actorId);
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Page must be >= 0 and size must be 1-100");
        if (submittedFrom != null && submittedTo != null && submittedFrom.isAfter(submittedTo)) {
            throw new IllegalArgumentException("submittedFrom must not be after submittedTo");
        }
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim().toLowerCase(Locale.ROOT);
        List<UnifiedServiceRequestView> filtered = all(serviceType, status).stream()
                .filter(view -> submittedFrom == null || !view.submittedAt().isBefore(submittedFrom))
                .filter(view -> submittedTo == null || !view.submittedAt().isAfter(submittedTo))
                .filter(view -> matches(view, normalizedQuery))
                .sorted(NEWEST_FIRST)
                .toList();
        int from = (int) Math.min((long) page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        int totalPages = filtered.isEmpty() ? 0 : (filtered.size() + size - 1) / size;
        return new OperationsPage<>(filtered.subList(from, to), page, size, filtered.size(), totalPages);
    }

    @Transactional(readOnly = true)
    public UnifiedServiceRequestView details(AccountId actorId, ServiceRequestType type, UUID requestId) {
        access.requireActiveOperator(actorId);
        return switch (type) {
            case PASSPORT -> passports.findById(requestId).map(this::view)
                    .orElseThrow(() -> new NotFoundException("Request not found"));
            case NATIONAL_IDENTITY -> identities.findById(requestId).map(this::view)
                    .orElseThrow(() -> new NotFoundException("Request not found"));
            case BIRTH_CERTIFICATE -> birthCertificates.findById(requestId).map(this::view)
                    .orElseThrow(() -> new NotFoundException("Request not found"));
        };
    }

    @Transactional(readOnly = true)
    public List<UnifiedRequestStatusChangeView> history(AccountId actorId, ServiceRequestType type, UUID requestId) {
        details(actorId, type, requestId);
        return switch (type) {
            case PASSPORT -> passportHistory.findByRequestId(requestId).stream()
                    .map(change -> new UnifiedRequestStatusChangeView(change.id(), change.requestId(),
                            status(change.fromStatus()), status(change.toStatus()), change.reason(),
                            change.changedBy().value(), change.changedAt())).toList();
            case NATIONAL_IDENTITY -> identityHistory.findByRequestId(requestId).stream()
                    .map(change -> new UnifiedRequestStatusChangeView(change.id(), change.requestId(),
                            status(change.fromStatus()), status(change.toStatus()), change.reason(),
                            change.changedBy().value(), change.changedAt())).toList();
            case BIRTH_CERTIFICATE -> birthHistory.findByRequestId(requestId).stream()
                    .map(change -> new UnifiedRequestStatusChangeView(change.id(), change.requestId(),
                            status(change.fromStatus()), status(change.toStatus()), change.reason(),
                            change.changedBy().value(), change.changedAt())).toList();
        };
    }

    @Transactional(readOnly = true)
    public OperationsDashboard dashboard(AccountId actorId) {
        access.requireActiveOperator(actorId);
        List<UnifiedServiceRequestView> requests = all(null, null).stream().sorted(NEWEST_FIRST).toList();
        EnumMap<ServiceRequestType, Long> byService = new EnumMap<>(ServiceRequestType.class);
        EnumMap<UnifiedRequestStatus, Long> byStatus = new EnumMap<>(UnifiedRequestStatus.class);
        for (ServiceRequestType type : ServiceRequestType.values()) byService.put(type, 0L);
        for (UnifiedRequestStatus status : UnifiedRequestStatus.values()) byStatus.put(status, 0L);
        requests.forEach(request -> {
            byService.compute(request.serviceType(), (key, value) -> value + 1);
            byStatus.compute(request.status(), (key, value) -> value + 1);
        });
        LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        long submittedToday = requests.stream().filter(request ->
                LocalDate.ofInstant(request.submittedAt(), ZoneOffset.UTC).equals(today)).count();
        return new OperationsDashboard(requests.size(), byStatus.get(UnifiedRequestStatus.SUBMITTED),
                byStatus.get(UnifiedRequestStatus.UNDER_REVIEW), byStatus.get(UnifiedRequestStatus.APPROVED),
                byStatus.get(UnifiedRequestStatus.REJECTED), submittedToday, byService, byStatus,
                requests.stream().limit(10).toList());
    }

    @Transactional(readOnly = true)
    public OperationsRequestReport report(AccountId actorId, Instant from, Instant to) {
        access.requireActiveOperator(actorId);
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
        List<UnifiedServiceRequestView> requests = all(null, null).stream()
                .filter(request -> from == null || !request.submittedAt().isBefore(from))
                .filter(request -> to == null || !request.submittedAt().isAfter(to))
                .toList();
        EnumMap<ServiceRequestType, Long> byService = new EnumMap<>(ServiceRequestType.class);
        EnumMap<UnifiedRequestStatus, Long> byStatus = new EnumMap<>(UnifiedRequestStatus.class);
        for (ServiceRequestType type : ServiceRequestType.values()) byService.put(type, 0L);
        for (UnifiedRequestStatus status : UnifiedRequestStatus.values()) byStatus.put(status, 0L);
        Map<LocalDate, Long> byDay = new TreeMap<>();
        long reviewedCount = 0;
        double totalReviewHours = 0;
        for (UnifiedServiceRequestView request : requests) {
            byService.compute(request.serviceType(), (key, value) -> value + 1);
            byStatus.compute(request.status(), (key, value) -> value + 1);
            byDay.merge(LocalDate.ofInstant(request.submittedAt(), ZoneOffset.UTC), 1L, Long::sum);
            if (request.reviewedAt() != null) {
                reviewedCount++;
                totalReviewHours += Duration.between(request.submittedAt(), request.reviewedAt()).toMinutes() / 60.0;
            }
        }
        double averageReviewHours = reviewedCount == 0 ? 0 : totalReviewHours / reviewedCount;
        return new OperationsRequestReport(from, to, requests.size(), byService, byStatus, byDay,
                Math.round(averageReviewHours * 100.0) / 100.0);
    }

    private List<UnifiedServiceRequestView> all(ServiceRequestType type, UnifiedRequestStatus status) {
        List<UnifiedServiceRequestView> result = new ArrayList<>();
        if (type == null || type == ServiceRequestType.PASSPORT) {
            for (PassportRequestStatus value : PassportRequestStatus.values()) {
                if (status == null || value.name().equals(status.name())) passports.findByStatus(value).stream().map(this::view).forEach(result::add);
            }
        }
        if (type == null || type == ServiceRequestType.NATIONAL_IDENTITY) {
            for (NationalIdentityRequestStatus value : NationalIdentityRequestStatus.values()) {
                if (status == null || value.name().equals(status.name())) identities.findByStatus(value).stream().map(this::view).forEach(result::add);
            }
        }
        if (type == null || type == ServiceRequestType.BIRTH_CERTIFICATE) {
            for (BirthCertificateRequestStatus value : BirthCertificateRequestStatus.values()) {
                if (status == null || value.name().equals(status.name())) birthCertificates.findByStatus(value).stream().map(this::view).forEach(result::add);
            }
        }
        return result;
    }

    private UnifiedServiceRequestView view(PassportRequest request) {
        return view(request.id(), ServiceRequestType.PASSPORT, request.kind().name(), request.status().name(),
                request.citizenId(), request.requestReason(), request.submissionDetails(), request.reviewedBy() == null ? null : request.reviewedBy().value(),
                request.submittedAt(), request.reviewedAt(), request.decisionReason());
    }

    private UnifiedServiceRequestView view(NationalIdentityRequest request) {
        return view(request.id(), ServiceRequestType.NATIONAL_IDENTITY, request.kind().name(), request.status().name(),
                request.citizenId(), request.requestReason(), request.submissionDetails(), request.reviewedBy() == null ? null : request.reviewedBy().value(),
                request.submittedAt(), request.reviewedAt(), request.decisionReason());
    }

    private UnifiedServiceRequestView view(BirthCertificateRequest request) {
        return view(request.id(), ServiceRequestType.BIRTH_CERTIFICATE, request.kind().name(), request.status().name(),
                request.citizenId(), request.requestReason(), null, request.reviewedBy() == null ? null : request.reviewedBy().value(),
                request.submittedAt(), request.reviewedAt(), request.decisionReason());
    }

    private UnifiedServiceRequestView view(UUID id, ServiceRequestType type, String kind, String status,
                                           CitizenId citizenId, String requestReason,
                                           com.chari.chariapp.request.domain.ServiceRequestSubmissionDetails submissionDetails,
                                           UUID reviewedBy,
                                           Instant submittedAt, Instant reviewedAt, String decisionReason) {
        Citizen citizen = citizens.findById(citizenId).orElseThrow(() -> new NotFoundException("Citizen not found"));
        return new UnifiedServiceRequestView(id, type, kind, UnifiedRequestStatus.valueOf(status), citizenId.value(),
                protector.decrypt(citizen.nationalId().ciphertext()), requestReason, submissionDetails, reviewedBy, submittedAt,
                reviewedAt, decisionReason);
    }

    private static boolean matches(UnifiedServiceRequestView view, String query) {
        if (query == null) return true;
        return view.id().toString().toLowerCase(Locale.ROOT).contains(query)
                || view.citizenNationalId().toLowerCase(Locale.ROOT).contains(query)
                || view.kind().toLowerCase(Locale.ROOT).contains(query)
                || view.serviceType().name().toLowerCase(Locale.ROOT).contains(query)
                || view.status().name().toLowerCase(Locale.ROOT).contains(query);
    }

    private static UnifiedRequestStatus status(Enum<?> status) {
        return status == null ? null : UnifiedRequestStatus.valueOf(status.name());
    }
}
