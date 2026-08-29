package com.dpc.smart_staffing_backend.service;

import com.dpc.smart_staffing_backend.dto.SkillDTO;
import com.dpc.smart_staffing_backend.dto.StaffingRequestDTO;
import com.dpc.smart_staffing_backend.dto.StaffingRequestRequestDTO;
import com.dpc.smart_staffing_backend.entity.Skill;
import com.dpc.smart_staffing_backend.entity.StaffingRequest;
import com.dpc.smart_staffing_backend.entity.StaffingRequestStatus;
import com.dpc.smart_staffing_backend.mapper.StaffingRequestMapper;
import com.dpc.smart_staffing_backend.repository.SkillRepository;
import com.dpc.smart_staffing_backend.repository.StaffingRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StaffingRequestServiceTest {

    private StaffingRequestRepository staffingRequestRepository;
    private SkillRepository skillRepository;
    private StaffingRequestMapper staffingRequestMapper;
    private StaffingRequestService staffingRequestService;

    @BeforeEach
    void setUp() {
        staffingRequestRepository = mock(StaffingRequestRepository.class);
        skillRepository = mock(SkillRepository.class);
        staffingRequestMapper = new StaffingRequestMapper();
        staffingRequestService = new StaffingRequestService(staffingRequestRepository, skillRepository, staffingRequestMapper);
    }

    @Test
    void createStaffingRequest_withNullStatus_defaultsToOpen() {
        StaffingRequestRequestDTO requestDTO = new StaffingRequestRequestDTO(
                "Java Architect",
                "Acme Corp",
                "Remote",
                5,
                "Need Java expert",
                null, // null status
                List.of(new SkillDTO(null, "Java", "Technical"))
        );

        when(skillRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.of(new Skill("Java", "Technical")));
        when(staffingRequestRepository.save(any(StaffingRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StaffingRequestDTO result = staffingRequestService.create(requestDTO);

        assertNotNull(result);
        assertEquals("Java Architect", result.title());
        assertEquals("Acme Corp", result.clientName());
        assertEquals(StaffingRequestStatus.OPEN, result.status());
        verify(staffingRequestRepository, times(1)).save(any(StaffingRequest.class));
    }
}
