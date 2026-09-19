package com.chari.chariapp.operations.infrastructure.web;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.operations.application.CitizenDetailsView;
import com.chari.chariapp.operations.application.CitizenDirectoryService;
import com.chari.chariapp.operations.application.CitizenSummaryView;
import com.chari.chariapp.operations.application.OperationsPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations/citizens")
public class OperationsCitizenController {
    private final CitizenDirectoryService service;

    public OperationsCitizenController(CitizenDirectoryService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_CITIZEN_VIEW')")
    public OperationsPage<CitizenSummaryView> list(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestParam(required = false) String query,
                                                   @RequestParam(required = false) AccountStatus status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return service.list(actor(jwt), query, status, page, size);
    }

    @GetMapping("/{citizenId}")
    @PreAuthorize("hasAuthority('PERM_CITIZEN_VIEW')")
    public CitizenDetailsView details(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId) {
        return service.details(actor(jwt), citizenId);
    }

    @PatchMapping("/{citizenId}/status")
    @PreAuthorize("hasAuthority('PERM_CITIZEN_EDIT')")
    public CitizenSummaryView updateStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID citizenId,
                                           @Valid @RequestBody StatusBody body) {
        return service.updateStatus(actor(jwt), citizenId, body.status());
    }

    private static AccountId actor(Jwt jwt) {
        return new AccountId(UUID.fromString(jwt.getSubject()));
    }

    public record StatusBody(@NotNull AccountStatus status) {
    }
}
