package com.chari.chariapp.home.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Small, non-sensitive read model for the citizen application's home screen. */
public record CitizenHome(
        long unreadNotificationCount,
        int openRequestCount,
        UpcomingAppointment upcomingAppointment,
        List<RecentRequest> recentRequests
) {
    public record UpcomingAppointment(UUID id, String department, String officeName, Instant startsAt, Instant endsAt) { }
    public record RecentRequest(UUID id, String serviceType, String kind, String status, Instant submittedAt) { }
}
