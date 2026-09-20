package com.chari.chariapp.operations.infrastructure.web;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.notification.domain.NotificationType;
import com.chari.chariapp.notification.domain.UserNotification;
import com.chari.chariapp.operations.application.OperationsNotificationService;
import com.chari.chariapp.operations.application.OperationsPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations/notifications")
public class OperationsNotificationController {
    private final OperationsNotificationService service;

    public OperationsNotificationController(OperationsNotificationService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_NOTIFICATION_VIEW')")
    public OperationsPage<UserNotification> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) UUID citizenId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(actor(jwt), citizenId, type, read, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERM_NOTIFICATION_SEND')")
    public UserNotification send(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SendNotificationBody body) {
        return service.send(actor(jwt), body.citizenId(), body.type(), body.title(), body.message());
    }

    private static AccountId actor(Jwt jwt) {
        return new AccountId(UUID.fromString(jwt.getSubject()));
    }

    public record SendNotificationBody(
            @NotNull UUID citizenId,
            @NotNull NotificationType type,
            @NotBlank @Size(max = 160) String title,
            @NotBlank @Size(max = 1000) String message
    ) {
    }
}
