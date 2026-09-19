package com.chari.chariapp.operations.infrastructure.web;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.operations.application.OperationsDashboard;
import com.chari.chariapp.operations.application.OperationsAuditService;
import com.chari.chariapp.operations.application.OperationsPage;
import com.chari.chariapp.operations.application.OperationsRequestQueryService;
import com.chari.chariapp.operations.application.OperationsRequestReport;
import com.chari.chariapp.shared.application.OperationalAuditEventView;
import com.chari.chariapp.operations.application.UnifiedServiceRequestView;
import com.chari.chariapp.operations.application.UnifiedRequestStatusChangeView;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.operations.domain.UnifiedRequestStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/operations")
public class OperationsDashboardController {
    private final OperationsRequestQueryService service;
    private final OperationsAuditService auditService;

    public OperationsDashboardController(OperationsRequestQueryService service,
                                         OperationsAuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_VIEW')")
    public OperationsDashboard dashboard(@AuthenticationPrincipal Jwt jwt) {
        return service.dashboard(actor(jwt));
    }

    @GetMapping("/reports/requests")
    @PreAuthorize("hasAuthority('PERM_REPORT_VIEW')")
    public OperationsRequestReport requestReport(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return service.report(actor(jwt), from, to);
    }

    @GetMapping("/audit-events")
    @PreAuthorize("hasAuthority('PERM_AUDIT_VIEW')")
    public OperationsPage<OperationalAuditEventView> auditEvents(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String actorAccountId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant occurredFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant occurredTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return auditService.list(actor(jwt), actorAccountId, action, targetType, targetId,
                occurredFrom, occurredTo, page, size);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAuthority('PERM_REQUEST_VIEW')")
    public OperationsPage<UnifiedServiceRequestView> requests(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ServiceRequestType serviceType,
            @RequestParam(required = false) UnifiedRequestStatus status,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant submittedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant submittedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(actor(jwt), serviceType, status, query, submittedFrom, submittedTo, page, size);
    }

    @GetMapping("/requests/{serviceType}/{requestId}")
    @PreAuthorize("hasAuthority('PERM_REQUEST_VIEW')")
    public UnifiedServiceRequestView details(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable ServiceRequestType serviceType,
                                             @PathVariable UUID requestId) {
        return service.details(actor(jwt), serviceType, requestId);
    }

    @GetMapping("/requests/{serviceType}/{requestId}/history")
    @PreAuthorize("hasAuthority('PERM_REQUEST_VIEW')")
    public List<UnifiedRequestStatusChangeView> history(@AuthenticationPrincipal Jwt jwt,
                                                        @PathVariable ServiceRequestType serviceType,
                                                        @PathVariable UUID requestId) {
        return service.history(actor(jwt), serviceType, requestId);
    }

    private static AccountId actor(Jwt jwt) { return new AccountId(UUID.fromString(jwt.getSubject())); }
}
