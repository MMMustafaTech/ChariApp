package com.chari.chariapp.document.infrastructure.web;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.document.application.MyDocumentsUseCase;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.document.domain.DependentBirthCertificate;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/me/documents")
public class MyDocumentsController {

    private final MyDocumentsUseCase myDocumentsUseCase;

    public MyDocumentsController(MyDocumentsUseCase myDocumentsUseCase) {
        this.myDocumentsUseCase = myDocumentsUseCase;
    }

    @GetMapping("/passport")
    public PassportResponse passport(@AuthenticationPrincipal Jwt jwt) {
        return PassportResponse.from(myDocumentsUseCase.getPassport(accountId(jwt)));
    }

    @GetMapping("/national-identity")
    public NationalIdentityResponse nationalIdentity(@AuthenticationPrincipal Jwt jwt) {
        return NationalIdentityResponse.from(myDocumentsUseCase.getNationalIdentity(accountId(jwt)));
    }

    @GetMapping("/birth-certificate")
    public MyBirthCertificate birthCertificate(@AuthenticationPrincipal Jwt jwt) {
        return myDocumentsUseCase.getBirthCertificate(accountId(jwt));
    }

    @GetMapping("/dependent-birth-certificates")
    public List<DependentBirthCertificate> dependentBirthCertificates(@AuthenticationPrincipal Jwt jwt) {
        return myDocumentsUseCase.getDependentBirthCertificates(accountId(jwt));
    }

    private static AccountId accountId(Jwt jwt) {
        return new AccountId(UUID.fromString(jwt.getSubject()));
    }

    public record PassportResponse(
            String passportNumber,
            String firstName,
            String lastName,
            LocalDate birthDate,
            String birthPlace,
            LocalDate issueDate,
            LocalDate expiryDate,
            String issuePlace,
            String issuingAuthority,
            String issueingAuthority,
            String profession,
            String nationality,
            String gender
    ) {
        static PassportResponse from(MyPassport passport) {
            return new PassportResponse(passport.passportNumber(), passport.firstName(), passport.lastName(),
                    passport.dateOfBirth(), passport.placeOfBirth(), passport.issuedOn(), passport.expiresOn(),
                    passport.placeOfIssue(), passport.issuingAuthority(), passport.issuingAuthority(),
                    passport.profession(), passport.nationality(), passport.sex());
        }
    }

    public record NationalIdentityResponse(
            String nationalId,
            String firstName,
            String lastName,
            String fatherName,
            String motherName,
            String gender,
            LocalDate dateOfBirth,
            LocalDate dateofBirth,
            String placeOfBirth,
            String address,
            String profession,
            String bloodGroup,
            String cardSerial,
            String issueDetails,
            LocalDate dateOfExpiry
    ) {
        static NationalIdentityResponse from(MyNationalIdentity identity) {
            String issuePlace = identity.placeOfIssue() == null ? "" : identity.placeOfIssue();
            String issueDate = identity.issuedOn() == null ? "" : identity.issuedOn().toString();
            String issueDetails = issuePlace.isEmpty() ? issueDate
                    : issueDate.isEmpty() ? issuePlace : issuePlace + "/" + issueDate;
            return new NationalIdentityResponse(identity.nationalId(), identity.firstName(), identity.lastName(),
                    identity.fatherName(), identity.motherName(), identity.gender(), identity.dateOfBirth(),
                    identity.dateOfBirth(), identity.placeOfBirth(), identity.address(), identity.profession(),
                    identity.bloodGroup(), identity.cardSerial(), issueDetails, identity.expiresOn());
        }
    }
}
