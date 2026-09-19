package com.chari.chariapp.account.infrastructure.web;

import com.chari.chariapp.account.application.ChangeEmployeeAccountStatusCommand;
import com.chari.chariapp.account.application.EmployeeAccountAdministrationUseCase;
import com.chari.chariapp.account.application.ProvisionEmployeeAccountCommand;
import com.chari.chariapp.account.application.ResetEmployeePasswordCommand;
import com.chari.chariapp.account.application.StaffAccountView;
import com.chari.chariapp.account.application.UpdateStaffAccountCommand;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.StaffPermission;
import com.chari.chariapp.citizen.domain.PhoneReference;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/staff-accounts")
public class EmployeeAccountAdministrationController {
    private final EmployeeAccountAdministrationUseCase administration;
    private final PersonalDataProtector dataProtector;

    public EmployeeAccountAdministrationController(EmployeeAccountAdministrationUseCase administration,
                                                    PersonalDataProtector dataProtector) {
        this.administration = administration;
        this.dataProtector = dataProtector;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_STAFF_MANAGE')")
    public ResponseEntity<StaffAccountResponse> provisionEmployee(
            @Valid @RequestBody ProvisionEmployeeRequest request, @AuthenticationPrincipal Jwt jwt) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        AccountId accountId = administration.provisionEmployee(new ProvisionEmployeeAccountCommand(
                administratorId(jwt), dataProtector.lookup(email), dataProtector.encrypt(email), request.password(),
                request.firstName(), request.lastName(), phone(request.phoneNumber()), request.jobTitle(), request.permissions()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new StaffAccountResponse(accountId.value(), AccountStatus.ACTIVE));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_STAFF_VIEW')")
    public List<StaffAccountDetailsResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return administration.listStaff(administratorId(jwt)).stream().map(this::response).toList();
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasAuthority('PERM_STAFF_VIEW')")
    public StaffAccountDetailsResponse get(@PathVariable UUID accountId, @AuthenticationPrincipal Jwt jwt) {
        return response(administration.getStaff(administratorId(jwt), new AccountId(accountId)));
    }

    @PatchMapping("/{accountId}")
    @PreAuthorize("hasAuthority('PERM_STAFF_MANAGE')")
    public ResponseEntity<Void> update(@PathVariable UUID accountId, @Valid @RequestBody UpdateStaffRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        administration.updateEmployee(new UpdateStaffAccountCommand(administratorId(jwt), new AccountId(accountId),
                request.firstName(), request.lastName(), phone(request.phoneNumber()), request.jobTitle(), request.permissions()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{accountId}/status")
    @PreAuthorize("hasAuthority('PERM_STAFF_MANAGE')")
    public ResponseEntity<Void> changeStatus(@PathVariable UUID accountId,
                                             @Valid @RequestBody ChangeStatusRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        administration.changeEmployeeStatus(new ChangeEmployeeAccountStatusCommand(
                administratorId(jwt), new AccountId(accountId), request.status()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{accountId}/password-reset")
    @PreAuthorize("hasAuthority('PERM_STAFF_MANAGE')")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID accountId,
                                              @Valid @RequestBody ResetPasswordRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        administration.resetEmployeePassword(new ResetEmployeePasswordCommand(
                administratorId(jwt), new AccountId(accountId), request.temporaryPassword()));
        return ResponseEntity.noContent().build();
    }

    private static AccountId administratorId(Jwt jwt) {
        return new AccountId(UUID.fromString(jwt.getSubject()));
    }

    private PhoneReference phone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) return null;
        String normalized = rawPhone.trim().replace(" ", "");
        return new PhoneReference(dataProtector.lookup(normalized), dataProtector.encrypt(normalized));
    }

    private StaffAccountDetailsResponse response(StaffAccountView view) {
        var account = view.account();
        var profile = view.profile();
        return new StaffAccountDetailsResponse(account.id().value(), dataProtector.decrypt(account.email().ciphertext()),
                account.status(), account.roles(), account.permissions(), profile == null ? null : profile.employeeNumber(),
                profile == null ? null : profile.firstName(), profile == null ? null : profile.lastName(),
                profile == null || profile.phone() == null ? null : dataProtector.decrypt(profile.phone().ciphertext()),
                profile == null ? null : profile.jobTitle());
    }

    public record ProvisionEmployeeRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 12, max = 128) String password,
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            @Size(max = 32) String phoneNumber,
            @Size(max = 120) String jobTitle,
            Set<StaffPermission> permissions
    ) {
    }

    public record UpdateStaffRequest(
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            @Size(max = 32) String phoneNumber,
            @Size(max = 120) String jobTitle,
            @NotNull Set<StaffPermission> permissions
    ) {
    }

    public record ChangeStatusRequest(@NotNull AccountStatus status) {
    }

    public record ResetPasswordRequest(@NotBlank @Size(min = 12, max = 128) String temporaryPassword) {
    }

    public record StaffAccountResponse(UUID accountId, AccountStatus status) {
    }

    public record StaffAccountDetailsResponse(
            UUID accountId, String email, AccountStatus status, Set<AccountRole> roles,
            Set<StaffPermission> permissions, String employeeNumber, String firstName,
            String lastName, String phoneNumber, String jobTitle
    ) {
    }
}
