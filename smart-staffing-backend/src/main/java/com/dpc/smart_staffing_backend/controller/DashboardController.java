package com.dpc.smart_staffing_backend.controller;

import com.dpc.smart_staffing_backend.dto.DashboardStatsDTO;
import com.dpc.smart_staffing_backend.dto.StaffingRequestDTO;
import com.dpc.smart_staffing_backend.entity.Availability;
import com.dpc.smart_staffing_backend.entity.StaffingRequestStatus;
import com.dpc.smart_staffing_backend.entity.InterviewStatus;
import com.dpc.smart_staffing_backend.mapper.StaffingRequestMapper;
import com.dpc.smart_staffing_backend.repository.InterviewRepository;
import com.dpc.smart_staffing_backend.repository.ConsultantRepository;
import com.dpc.smart_staffing_backend.repository.StaffingRequestRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Transactional(readOnly = true)
public class DashboardController {

    private final ConsultantRepository consultantRepository;
    private final StaffingRequestRepository staffingRequestRepository;
    private final InterviewRepository interviewRepository;
    private final StaffingRequestMapper staffingRequestMapper;

    public DashboardController(ConsultantRepository consultantRepository,
                               StaffingRequestRepository staffingRequestRepository,
                               InterviewRepository interviewRepository,
                               StaffingRequestMapper staffingRequestMapper) {
        this.consultantRepository = consultantRepository;
        this.staffingRequestRepository = staffingRequestRepository;
        this.interviewRepository = interviewRepository;
        this.staffingRequestMapper = staffingRequestMapper;
    }

    @GetMapping("/stats")
    public DashboardStatsDTO getStats() {
        long totalConsultants = consultantRepository.count();
        long availableConsultants = consultantRepository.countByAvailability(Availability.AVAILABLE);
        long activeRequests = staffingRequestRepository.countByStatusNot(StaffingRequestStatus.CLOSED);
        long scheduledInterviews = interviewRepository.countByStatus(InterviewStatus.SCHEDULED);

        List<StaffingRequestDTO> recentRequests = staffingRequestRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(staffingRequestMapper::toDTO)
                .toList();

        return new DashboardStatsDTO(
                totalConsultants,
                availableConsultants,
                activeRequests,
                scheduledInterviews,
                recentRequests
        );
    }
}
