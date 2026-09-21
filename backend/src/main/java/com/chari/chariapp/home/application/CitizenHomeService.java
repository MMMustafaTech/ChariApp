package com.chari.chariapp.home.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.appointment.application.port.out.AppointmentStore;
import com.chari.chariapp.appointment.domain.Appointment;
import com.chari.chariapp.appointment.domain.AppointmentStatus;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStore;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequest;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStore;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequest;
import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.request.domain.PassportRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CitizenHomeService {
    private static final int RECENT_REQUEST_LIMIT = 10;
    private final PassportRequestActorAccess access;
    private final PassportRequestStore passportRequests;
    private final NationalIdentityRequestStore identityRequests;
    private final BirthCertificateRequestStore birthRequests;
    private final AppointmentStore appointments;
    private final NotificationService notifications;
    private final Clock clock;

    public CitizenHomeService(PassportRequestActorAccess access, PassportRequestStore passportRequests,
                              NationalIdentityRequestStore identityRequests, BirthCertificateRequestStore birthRequests,
                              AppointmentStore appointments, NotificationService notifications, Clock clock) {
        this.access=Objects.requireNonNull(access); this.passportRequests=Objects.requireNonNull(passportRequests);
        this.identityRequests=Objects.requireNonNull(identityRequests); this.birthRequests=Objects.requireNonNull(birthRequests);
        this.appointments=Objects.requireNonNull(appointments); this.notifications=Objects.requireNonNull(notifications); this.clock=Objects.requireNonNull(clock);
    }

    public CitizenHome home(AccountId accountId) {
        CitizenId citizen = access.requireActiveCitizen(accountId);
        List<PassportRequest> passports = passportRequests.findByCitizenId(citizen);
        List<NationalIdentityRequest> identities = identityRequests.findByCitizenId(citizen);
        List<BirthCertificateRequest> births = birthRequests.findByCitizenId(citizen);
        List<CitizenHome.RecentRequest> recent = Stream.concat(Stream.concat(passports.stream().map(this::passportSummary), identities.stream().map(this::identitySummary)), births.stream().map(this::birthSummary))
                .sorted(Comparator.comparing(CitizenHome.RecentRequest::submittedAt).reversed()).limit(RECENT_REQUEST_LIMIT).toList();
        int open = (int) recentAll(passports, identities, births).filter(RequestState::open).count();
        Instant now = Instant.now(clock);
        CitizenHome.UpcomingAppointment upcoming = appointments.findByCitizenId(citizen).stream()
                .filter(appointment -> appointment.status() == AppointmentStatus.BOOKED && appointment.startsAt().isAfter(now))
                .min(Comparator.comparing(Appointment::startsAt)).map(this::appointmentSummary).orElse(null);
        return new CitizenHome(notifications.unreadCount(accountId), open, upcoming, recent);
    }

    private Stream<RequestState> recentAll(List<PassportRequest> passports, List<NationalIdentityRequest> identities, List<BirthCertificateRequest> births) {
        return Stream.concat(Stream.concat(passports.stream().map(request -> new RequestState(request.status().isOpen())), identities.stream().map(request -> new RequestState(request.status().isOpen()))), births.stream().map(request -> new RequestState(request.status().isOpen())));
    }
    private CitizenHome.RecentRequest passportSummary(PassportRequest request) { return new CitizenHome.RecentRequest(request.id(), "PASSPORT", request.kind().name(), request.status().name(), request.submittedAt()); }
    private CitizenHome.RecentRequest identitySummary(NationalIdentityRequest request) { return new CitizenHome.RecentRequest(request.id(), "NATIONAL_IDENTITY", request.kind().name(), request.status().name(), request.submittedAt()); }
    private CitizenHome.RecentRequest birthSummary(BirthCertificateRequest request) { return new CitizenHome.RecentRequest(request.id(), "BIRTH_CERTIFICATE", request.kind().name(), request.status().name(), request.submittedAt()); }
    private CitizenHome.UpcomingAppointment appointmentSummary(Appointment appointment) { return new CitizenHome.UpcomingAppointment(appointment.id(), appointment.serviceType().appointmentDepartment().name(), appointment.officeName(), appointment.startsAt(), appointment.endsAt()); }
    private record RequestState(boolean open) { }
}
