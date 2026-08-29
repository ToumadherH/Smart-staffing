package com.dpc.smart_staffing_backend.dto;

import java.util.List;

public record DashboardStatsDTO(
        long totalConsultants,
        long availableConsultants,
        long activeRequests,
        long upcomingInterviews,
        List<StaffingRequestDTO> recentRequests
) {}
