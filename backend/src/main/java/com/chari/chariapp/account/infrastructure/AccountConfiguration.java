package com.chari.chariapp.account.infrastructure;

import com.chari.chariapp.account.application.CreateAccountService;
import com.chari.chariapp.account.application.CreateAccountUseCase;
import com.chari.chariapp.account.application.CitizenProfileService;
import com.chari.chariapp.account.application.CitizenSecurityService;
import com.chari.chariapp.account.application.EmployeeAccountAdministrationService;
import com.chari.chariapp.account.application.EmployeeAccountAdministrationUseCase;
import com.chari.chariapp.account.application.CreateVerifiedCitizenAccountService;
import com.chari.chariapp.account.application.CreateVerifiedCitizenAccountUseCase;
import com.chari.chariapp.account.application.LoginService;
import com.chari.chariapp.account.application.RefreshTokenService;
import com.chari.chariapp.account.application.RequestEnrollmentOtpService;
import com.chari.chariapp.account.application.RequestEnrollmentOtpUseCase;
import com.chari.chariapp.account.application.VerifyEnrollmentOtpService;
import com.chari.chariapp.account.application.VerifyEnrollmentOtpUseCase;
import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.AccessTokenIssuer;
import com.chari.chariapp.account.application.port.out.EnrollmentChallengeStore;
import com.chari.chariapp.account.application.port.out.OtpCodeGenerator;
import com.chari.chariapp.account.application.port.out.OtpSender;
import com.chari.chariapp.account.application.port.out.PasswordHasher;
import com.chari.chariapp.account.application.port.out.RefreshSessionStore;
import com.chari.chariapp.account.application.port.out.RefreshTokenGenerator;
import com.chari.chariapp.account.application.port.out.RefreshTokenHasher;
import com.chari.chariapp.account.application.port.out.VerificationCodeHasher;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AccountConfiguration {

    @Bean
    public LoginService loginService(
            AccountStore accountStore,
            PasswordHasher passwordHasher,
            RefreshSessionStore refreshSessionStore,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenHasher refreshTokenHasher,
            AccessTokenIssuer accessTokenIssuer,
            Clock clock
    ) {
        return new LoginService(
                accountStore, passwordHasher, refreshSessionStore, refreshTokenGenerator,
                refreshTokenHasher, accessTokenIssuer, clock
        );
    }

    @Bean
    public RefreshTokenService refreshTokenService(
            AccountStore accountStore,
            RefreshSessionStore refreshSessionStore,
            RefreshTokenHasher refreshTokenHasher,
            LoginService loginService,
            Clock clock
    ) {
        return new RefreshTokenService(accountStore, refreshSessionStore, refreshTokenHasher, loginService, clock);
    }

    @Bean
    public CreateAccountUseCase createAccountUseCase(
            AccountStore accountStore,
            PasswordHasher passwordHasher,
            Clock clock
    ) {
        return new CreateAccountService(accountStore, passwordHasher, clock);
    }

    @Bean
    public EmployeeAccountAdministrationUseCase employeeAccountAdministrationUseCase(
            AccountStore accountStore,
            PasswordHasher passwordHasher,
            OperationalAuditStore auditStore,
            Clock clock
    ) {
        return new EmployeeAccountAdministrationService(accountStore, passwordHasher, auditStore, clock);
    }

    @Bean
    public CreateVerifiedCitizenAccountUseCase createVerifiedCitizenAccountUseCase(
            AccountStore accountStore,
            EnrollmentChallengeStore challengeStore,
            PasswordHasher passwordHasher,
            Clock clock
    ) {
        return new CreateVerifiedCitizenAccountService(accountStore, challengeStore, passwordHasher, clock);
    }

    @Bean
    public CitizenProfileService citizenProfileService(
            AccountStore accountStore, CitizenStore citizenStore, PersonalDataProtector dataProtector
    ) {
        return new CitizenProfileService(accountStore, citizenStore, dataProtector);
    }

    @Bean
    public CitizenSecurityService citizenSecurityService(
            AccountStore accountStore,
            PasswordHasher passwordHasher,
            RefreshSessionStore refreshSessionStore,
            OperationalAuditStore auditStore,
            Clock clock
    ) {
        return new CitizenSecurityService(accountStore, passwordHasher, refreshSessionStore, auditStore, clock);
    }

    @Bean
    public RequestEnrollmentOtpUseCase requestEnrollmentOtpUseCase(
            CitizenStore citizenStore,
            AccountStore accountStore,
            EnrollmentChallengeStore challengeStore,
            OtpCodeGenerator codeGenerator,
            VerificationCodeHasher codeHasher,
            OtpSender otpSender,
            Clock clock
    ) {
        return new RequestEnrollmentOtpService(
                citizenStore, accountStore, challengeStore, codeGenerator, codeHasher, otpSender, clock
        );
    }

    @Bean
    public VerifyEnrollmentOtpUseCase verifyEnrollmentOtpUseCase(
            EnrollmentChallengeStore challengeStore,
            CitizenStore citizenStore,
            VerificationCodeHasher codeHasher,
            Clock clock
    ) {
        return new VerifyEnrollmentOtpService(challengeStore, citizenStore, codeHasher, clock);
    }
}
