package com.chari.chariapp.appointment.infrastructure.web;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.appointment.application.*;
import com.chari.chariapp.appointment.domain.*;
import com.chari.chariapp.operations.application.OperationsPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AppointmentController {
    private final AppointmentSlotService slotService;
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentSlotService slotService, AppointmentService appointmentService) {
        this.slotService = slotService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/me/appointment-slots")
    public List<AppointmentSlot> availableSlots(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam AppointmentServiceType department) {
        return slotService.available(accountId(jwt), department);
    }

    @PostMapping("/me/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment book(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Booking body) {
        return appointmentService.book(accountId(jwt), body.slotId());
    }

    @GetMapping("/me/appointments")
    public List<Appointment> mine(@AuthenticationPrincipal Jwt jwt) {
        return appointmentService.mine(accountId(jwt));
    }

    @PostMapping("/me/appointments/{appointmentId}/cancel")
    public Appointment cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID appointmentId) {
        return appointmentService.cancelMine(accountId(jwt), appointmentId);
    }

    @PostMapping("/operations/appointment-slots")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_MANAGE')")
    public AppointmentSlot createSlot(@AuthenticationPrincipal Jwt jwt,
                                      @Valid @RequestBody SlotSubmission body) {
        return slotService.create(accountId(jwt), body.department(), body.officeName(),
                body.startsAt(), body.endsAt(), body.capacity());
    }

    @GetMapping("/operations/appointment-slots")
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_VIEW')")
    public OperationsPage<AppointmentSlot> slots(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestParam(required = false) AppointmentServiceType department,
                                                  @RequestParam(required = false) Boolean active,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startsFrom,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startsTo,
                                                  @RequestParam(required = false) String office,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return slotService.list(accountId(jwt), department, active, startsFrom, startsTo, office, page, size);
    }

    @PatchMapping("/operations/appointment-slots/{slotId}")
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_MANAGE')")
    public AppointmentSlot updateSlot(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID slotId,
                                      @Valid @RequestBody SlotSubmission body) {
        return slotService.update(accountId(jwt), slotId, body.department(), body.officeName(),
                body.startsAt(), body.endsAt(), body.capacity());
    }

    @PostMapping("/operations/appointment-slots/{slotId}/deactivate")
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_MANAGE')")
    public AppointmentSlot deactivateSlot(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID slotId) {
        return slotService.deactivate(accountId(jwt), slotId);
    }

    @GetMapping("/operations/appointments")
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_VIEW')")
    public List<Appointment> operationalAppointments(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam(defaultValue = "BOOKED") AppointmentStatus status) {
        return appointmentService.byStatus(accountId(jwt), status);
    }

    @GetMapping("/operations/appointments/search")
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_VIEW')")
    public OperationsPage<AppointmentOperationsView> searchAppointments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) AppointmentServiceType department,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startsFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startsTo,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return appointmentService.search(accountId(jwt), status, department, startsFrom, startsTo,
                query, page, size);
    }

    @PostMapping("/operations/appointments/{appointmentId}/complete")
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_MANAGE')")
    public Appointment complete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID appointmentId) {
        return appointmentService.complete(accountId(jwt), appointmentId);
    }

    @PostMapping("/operations/appointments/{appointmentId}/cancel")
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_MANAGE')")
    public Appointment cancelByOperator(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable UUID appointmentId) {
        return appointmentService.cancelByOperator(accountId(jwt), appointmentId);
    }

    private static AccountId accountId(Jwt jwt) {
        return new AccountId(UUID.fromString(jwt.getSubject()));
    }

    public record Booking(@NotNull UUID slotId) {
    }

    public record SlotSubmission(@NotNull AppointmentServiceType department,
                                 @NotBlank @Size(max = 160) String officeName,
                                 @NotNull Instant startsAt,
                                 @NotNull Instant endsAt,
                                 @Min(1) @Max(500) int capacity) {
    }
}
