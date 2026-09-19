package com.chari.chariapp.account.infrastructure.web;

import com.chari.chariapp.account.application.StaffAccountView;
import com.chari.chariapp.account.application.StaffSelfService;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.StaffPermission;
import com.chari.chariapp.citizen.domain.PhoneReference;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff/me")
public class StaffSelfController {
    private final StaffSelfService service;
    private final PersonalDataProtector protector;

    public StaffSelfController(StaffSelfService service, PersonalDataProtector protector) {
        this.service = service; this.protector = protector;
    }

    @GetMapping
    public ProfileResponse get(@AuthenticationPrincipal Jwt jwt) { return response(service.get(id(jwt))); }

    @PatchMapping
    public ProfileResponse update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
        PhoneReference phone = request.phoneNumber() == null || request.phoneNumber().isBlank() ? null
                : new PhoneReference(protector.lookup(request.phoneNumber().trim().replace(" ", "")),
                protector.encrypt(request.phoneNumber().trim().replace(" ", "")));
        service.update(id(jwt), request.firstName(), request.lastName(), phone);
        return response(service.get(id(jwt)));
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(id(jwt), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal Jwt jwt) {
        service.logoutAll(id(jwt));
        return ResponseEntity.noContent().build();
    }

    private ProfileResponse response(StaffAccountView view) {
        var account = view.account(); var profile = view.profile();
        return new ProfileResponse(account.id().value(), protector.decrypt(account.email().ciphertext()), account.status(),
                account.roles(), account.permissions(), profile == null ? null : profile.employeeNumber(),
                profile == null ? null : profile.firstName(), profile == null ? null : profile.lastName(),
                profile == null || profile.phone() == null ? null : protector.decrypt(profile.phone().ciphertext()),
                profile == null ? null : profile.jobTitle());
    }

    private static AccountId id(Jwt jwt) { return new AccountId(UUID.fromString(jwt.getSubject())); }

    public record UpdateProfileRequest(@Size(max=100) String firstName, @Size(max=100) String lastName,
                                       @Size(max=32) String phoneNumber) { }
    public record ChangePasswordRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min=12,max=128) String newPassword) { }
    public record ProfileResponse(UUID accountId, String email, AccountStatus status, Set<AccountRole> roles,
                                  Set<StaffPermission> permissions, String employeeNumber, String firstName,
                                  String lastName, String phoneNumber, String jobTitle) { }
}
