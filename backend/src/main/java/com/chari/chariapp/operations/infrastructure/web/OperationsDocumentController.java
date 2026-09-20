package com.chari.chariapp.operations.infrastructure.web;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.document.domain.MyBirthCertificate;
import com.chari.chariapp.document.domain.MyNationalIdentity;
import com.chari.chariapp.document.domain.MyPassport;
import com.chari.chariapp.operations.application.OperationsDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations/citizens/{citizenId}/documents")
public class OperationsDocumentController {
    private final OperationsDocumentService service;

    public OperationsDocumentController(OperationsDocumentService service) { this.service = service; }

    @GetMapping("/passport") @PreAuthorize("hasAuthority('PERM_PASSPORT_VIEW')")
    public MyPassport passport(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId) { return service.passport(actor(jwt), citizenId); }
    @PostMapping("/passport") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('PERM_PASSPORT_CREATE')")
    public MyPassport createPassport(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId, @RequestBody MyPassport body) { return service.createPassport(actor(jwt), citizenId, body); }
    @PutMapping("/passport") @PreAuthorize("hasAuthority('PERM_PASSPORT_EDIT')")
    public MyPassport updatePassport(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId, @RequestBody MyPassport body) { return service.updatePassport(actor(jwt), citizenId, body); }

    @GetMapping("/national-identity") @PreAuthorize("hasAuthority('PERM_NATIONAL_ID_VIEW')")
    public MyNationalIdentity nationalIdentity(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId) { return service.nationalIdentity(actor(jwt), citizenId); }
    @PostMapping("/national-identity") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('PERM_NATIONAL_ID_CREATE')")
    public MyNationalIdentity createNationalIdentity(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId, @RequestBody MyNationalIdentity body) { return service.createNationalIdentity(actor(jwt), citizenId, body); }
    @PutMapping("/national-identity") @PreAuthorize("hasAuthority('PERM_NATIONAL_ID_EDIT')")
    public MyNationalIdentity updateNationalIdentity(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId, @RequestBody MyNationalIdentity body) { return service.updateNationalIdentity(actor(jwt), citizenId, body); }

    @GetMapping("/birth-certificate") @PreAuthorize("hasAuthority('PERM_BIRTH_CERTIFICATE_VIEW')")
    public MyBirthCertificate birthCertificate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId) { return service.birthCertificate(actor(jwt), citizenId); }
    @PostMapping("/birth-certificate") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('PERM_BIRTH_CERTIFICATE_CREATE')")
    public MyBirthCertificate createBirthCertificate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId, @RequestBody MyBirthCertificate body) { return service.createBirthCertificate(actor(jwt), citizenId, body); }
    @PutMapping("/birth-certificate") @PreAuthorize("hasAuthority('PERM_BIRTH_CERTIFICATE_EDIT')")
    public MyBirthCertificate updateBirthCertificate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId, @RequestBody MyBirthCertificate body) { return service.updateBirthCertificate(actor(jwt), citizenId, body); }

    private static AccountId actor(Jwt jwt) { return new AccountId(UUID.fromString(jwt.getSubject())); }
}
