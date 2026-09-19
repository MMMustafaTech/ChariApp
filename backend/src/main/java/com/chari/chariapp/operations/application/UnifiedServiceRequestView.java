package com.chari.chariapp.operations.application;

import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.operations.domain.UnifiedRequestStatus;
import com.chari.chariapp.request.domain.ServiceRequestSubmissionDetails;

import java.time.Instant;
import java.util.UUID;

public record UnifiedServiceRequestView(
        UUID id,
        ServiceRequestType serviceType,
        String kind,
        UnifiedRequestStatus status,
        UUID citizenId,
        String citizenNationalId,
        String requestReason,
        ServiceRequestSubmissionDetails submissionDetails,
        UUID reviewedBy,
        Instant submittedAt,
        Instant reviewedAt,
        String decisionReason
) {
}
