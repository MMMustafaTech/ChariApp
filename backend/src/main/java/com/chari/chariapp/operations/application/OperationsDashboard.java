package com.chari.chariapp.operations.application;

import java.util.List;
import java.util.Map;

import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.operations.domain.UnifiedRequestStatus;

public record OperationsDashboard(
        long totalRequests,
        long submitted,
        long underReview,
        long approved,
        long rejected,
        long submittedToday,
        Map<ServiceRequestType, Long> requestsByService,
        Map<UnifiedRequestStatus, Long> requestsByStatus,
        List<UnifiedServiceRequestView> recentRequests
) {
    public OperationsDashboard {
        requestsByService = Map.copyOf(requestsByService);
        requestsByStatus = Map.copyOf(requestsByStatus);
        recentRequests = List.copyOf(recentRequests);
    }
}
