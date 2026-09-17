package com.chari.chariapp.account.infrastructure.web;

import com.chari.chariapp.account.application.CreateVerifiedCitizenAccountCommand;
import com.chari.chariapp.account.application.CreateVerifiedCitizenAccountUseCase;
import com.chari.chariapp.account.application.RequestEnrollmentOtpCommand;
import com.chari.chariapp.account.application.RequestEnrollmentOtpUseCase;
import com.chari.chariapp.account.application.VerifyEnrollmentOtpCommand;
import com.chari.chariapp.account.application.VerifyEnrollmentOtpUseCase;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.EnrollmentChallengeId;
import com.chari.chariapp.citizen.domain.PhoneReference;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import com.chari.chariapp.shared.security.PersonalDataNormalizer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/auth/enrollment")
public class EnrollmentController {

    private final RequestEnrollmentOtpUseCase requestEnrollmentOtp;
    private final VerifyEnrollmentOtpUseCase verifyEnrollmentOtp;
    private final CreateVerifiedCitizenAccountUseCase createVerifiedCitizenAccount;
    private final PersonalDataProtector dataProtector;

    public EnrollmentController(
            RequestEnrollmentOtpUseCase requestEnrollmentOtp,
            VerifyEnrollmentOtpUseCase verifyEnrollmentOtp,
            CreateVerifiedCitizenAccountUseCase createVerifiedCitizenAccount,
            PersonalDataProtector dataProtector
    ) {
        this.requestEnrollmentOtp = requestEnrollmentOtp;
        this.verifyEnrollmentOtp = verifyEnrollmentOtp;
        this.createVerifiedCitizenAccount = createVerifiedCitizenAccount;
        this.dataProtector = dataProtector;
    }

    @PostMapping("/otp")
    public ResponseEntity<EnrollmentOtpRequestResponse> requestOtp(
            @Valid @RequestBody EnrollmentOtpRequest request
    ) {
        String normalizedPhone = PersonalDataNormalizer.phone(request.phoneNumber());
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        var challengeId = requestEnrollmentOtp.request(new RequestEnrollmentOtpCommand(
                dataProtector.lookup(PersonalDataNormalizer.nationalId(request.nationalId())),
                new PhoneReference(
                        dataProtector.lookup(normalizedPhone),
                        dataProtector.encrypt(normalizedPhone)
                ),
                dataProtector.lookup(normalizedEmail)
        ));
        return ResponseEntity.accepted().body(new EnrollmentOtpRequestResponse(challengeId.value()));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Void> verifyOtp(@Valid @RequestBody EnrollmentOtpVerificationRequest request) {
        verifyEnrollmentOtp.verify(new VerifyEnrollmentOtpCommand(
                new EnrollmentChallengeId(request.challengeId()), request.code()
        ));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accounts")
    public ResponseEntity<EnrollmentAccountResponse> createAccount(
            @Valid @RequestBody EnrollmentAccountRequest request
    ) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        AccountId accountId = createVerifiedCitizenAccount.create(new CreateVerifiedCitizenAccountCommand(
                new EnrollmentChallengeId(request.challengeId()),
                dataProtector.lookup(normalizedEmail),
                dataProtector.encrypt(normalizedEmail),
                request.password()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(new EnrollmentAccountResponse(accountId.value()));
    }

    public record EnrollmentOtpRequest(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9 -]{6,32}") String nationalId,
            @NotBlank @Pattern(regexp = "\\+[1-9]\\d{7,14}") String phoneNumber,
            @NotBlank @Email @Size(max = 254) String email
    ) {
    }

    public record EnrollmentOtpRequestResponse(UUID challengeId) {
    }

    public record EnrollmentOtpVerificationRequest(
            @NotNull UUID challengeId,
            @NotBlank @Pattern(regexp = "\\d{6}") String code
    ) {
    }

    public record EnrollmentAccountRequest(
            @NotNull UUID challengeId,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 12, max = 128) String password
    ) {
    }

    public record EnrollmentAccountResponse(UUID accountId) {
    }
}
