package com.dpc.smart_staffing_backend.service;

import com.dpc.smart_staffing_backend.dto.ConsultantMatchDTO;
import com.dpc.smart_staffing_backend.entity.Availability;
import com.dpc.smart_staffing_backend.entity.Consultant;
import com.dpc.smart_staffing_backend.entity.Skill;
import com.dpc.smart_staffing_backend.entity.StaffingRequest;
import com.dpc.smart_staffing_backend.entity.StaffingRequestStatus;
import com.dpc.smart_staffing_backend.mapper.ConsultantMapper;
import com.dpc.smart_staffing_backend.repository.ConsultantRepository;
import com.dpc.smart_staffing_backend.repository.StaffingRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiMatchingServiceTest {

    private StaffingRequestRepository staffingRequestRepository;
    private ConsultantRepository consultantRepository;
    private ConsultantMapper consultantMapper;
    private AiMatchingService aiMatchingService;

    @BeforeEach
    void setUp() {
        staffingRequestRepository = mock(StaffingRequestRepository.class);
        consultantRepository = mock(ConsultantRepository.class);
        consultantMapper = new ConsultantMapper();
        aiMatchingService = new AiMatchingService(staffingRequestRepository, consultantRepository, consultantMapper);
    }

    @Test
    void findMatchesForRequest_calculatesCorrectMatchScores() {
        StaffingRequest request = new StaffingRequest(
                "Full Stack Developer", "TechCorp", "Tunis",
                3, "Full stack role", StaffingRequestStatus.OPEN
        );
        Skill java = new Skill("Java", "Technical");
        Skill angular = new Skill("Angular", "Technical");
        request.setRequiredSkills(Set.of(java, angular));

        Consultant consultant = new Consultant(
                "John Doe", "john@example.com", "12345678", 5, Availability.AVAILABLE, null, "Tunis"
        );
        consultant.setSkills(Set.of(java, angular));

        when(staffingRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(consultantRepository.findAll()).thenReturn(List.of(consultant));

        List<ConsultantMatchDTO> matches = aiMatchingService.findMatchesForRequest(1L);

        assertEquals(1, matches.size());
        ConsultantMatchDTO match = matches.get(0);
        assertEquals(100.0, match.matchScore());
        assertEquals(100.0, match.matchingScore());
        assertTrue(match.matchedSkills().contains("Java"));
        assertTrue(match.matchedSkills().contains("Angular"));
        assertTrue(match.missingSkills().isEmpty());
    }
}
