package com.chari.chariapp.operations.application;

import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.operations.domain.UnifiedRequestStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public record OperationsRequestReport(
        Instant from,
        Instant to,
        long totalRequests,
        Map<ServiceRequestType, Long> requestsByService,
        Map<UnifiedRequestStatus, Long> requestsByStatus,
        Map<LocalDate, Long> submissionsByDay,
        double averageReviewHours
) {
    public OperationsRequestReport {
        requestsByService = Map.copyOf(requestsByService);
        requestsByStatus = Map.copyOf(requestsByStatus);
        submissionsByDay = Map.copyOf(submissionsByDay);
    }
}
