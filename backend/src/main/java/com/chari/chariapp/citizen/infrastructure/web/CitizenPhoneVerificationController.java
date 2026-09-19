package com.chari.chariapp.citizen.infrastructure.web;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.EnrollmentChallengeId;
import com.chari.chariapp.citizen.application.ConfirmCitizenPhoneVerificationCommand;
import com.chari.chariapp.citizen.application.ConfirmCitizenPhoneVerificationUseCase;
import com.chari.chariapp.citizen.application.RequestCitizenPhoneVerificationCommand;
import com.chari.chariapp.citizen.application.RequestCitizenPhoneVerificationUseCase;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.PhoneReference;
import com.chari.chariapp.shared.security.PersonalDataNormalizer;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations/citizens")
public class CitizenPhoneVerificationController {
    private final RequestCitizenPhoneVerificationUseCase requestPhoneVerification;
    private final ConfirmCitizenPhoneVerificationUseCase confirmPhoneVerification;
    private final PersonalDataProtector dataProtector;

    public CitizenPhoneVerificationController(
            RequestCitizenPhoneVerificationUseCase requestPhoneVerification,
            ConfirmCitizenPhoneVerificationUseCase confirmPhoneVerification,
            PersonalDataProtector dataProtector
    ) {
        this.requestPhoneVerification = requestPhoneVerification;
        this.confirmPhoneVerification = confirmPhoneVerification;
        this.dataProtector = dataProtector;
    }

    @PostMapping("/{citizenId}/phone-verifications")
    @PreAuthorize("hasAuthority('PERM_CITIZEN_EDIT')")
    public ResponseEntity<PhoneVerificationRequestResponse> request(
            @PathVariable UUID citizenId,
            @Valid @RequestBody PhoneVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String phone = PersonalDataNormalizer.phone(request.phone());
        var challengeId = requestPhoneVerification.request(new RequestCitizenPhoneVerificationCommand(
                new CitizenId(citizenId),
                new PhoneReference(dataProtector.lookup(phone), dataProtector.encrypt(phone)),
                new AccountId(UUID.fromString(jwt.getSubject()))
        ));
        return ResponseEntity.accepted().body(new PhoneVerificationRequestResponse(challengeId.value()));
    }

    @PostMapping("/{citizenId}/phone-verifications/{challengeId}/confirm")
    @PreAuthorize("hasAuthority('PERM_CITIZEN_EDIT')")
    public ResponseEntity<Void> confirm(
            @PathVariable UUID citizenId,
            @PathVariable UUID challengeId,
            @Valid @RequestBody PhoneVerificationConfirmation request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        confirmPhoneVerification.confirm(new ConfirmCitizenPhoneVerificationCommand(
                new CitizenId(citizenId), new EnrollmentChallengeId(challengeId), request.code(),
                new AccountId(UUID.fromString(jwt.getSubject()))
        ));
        return ResponseEntity.noContent().build();
    }

    public record PhoneVerificationRequest(@NotBlank @Pattern(regexp = "\\+[1-9]\\d{7,14}") String phone) {
    }

    public record PhoneVerificationRequestResponse(UUID challengeId) {
    }

    public record PhoneVerificationConfirmation(@NotBlank @Pattern(regexp = "\\d{6}") String code) {
    }
}
