package com.chari.chariapp.settings.infrastructure.web;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.settings.application.SystemSettingsService;
import com.chari.chariapp.settings.domain.SystemSettings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations/settings")
public class SystemSettingsController {
    private final SystemSettingsService service;

    public SystemSettingsController(SystemSettingsService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_SETTINGS_MANAGE')")
    public SystemSettings get(@AuthenticationPrincipal Jwt jwt) {
        return service.get(actor(jwt));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PERM_SETTINGS_MANAGE')")
    public SystemSettings update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateSettingsBody body) {
        return service.update(actor(jwt), body.requestSubmissionsEnabled(), body.notificationsEnabled(),
                body.maintenanceMessage());
    }

    private static AccountId actor(Jwt jwt) {
        return new AccountId(UUID.fromString(jwt.getSubject()));
    }

    public record UpdateSettingsBody(
            @NotNull Boolean requestSubmissionsEnabled,
            @NotNull Boolean notificationsEnabled,
            @Size(max = 500) String maintenanceMessage
    ) {
    }
}
