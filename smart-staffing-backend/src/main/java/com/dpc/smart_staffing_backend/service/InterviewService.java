package com.dpc.smart_staffing_backend.service;

import com.dpc.smart_staffing_backend.dto.ConsultantResponseDTO;
import com.dpc.smart_staffing_backend.dto.InterviewDTO;
import com.dpc.smart_staffing_backend.dto.InterviewRequestDTO;
import com.dpc.smart_staffing_backend.entity.Consultant;
import com.dpc.smart_staffing_backend.entity.Interview;
import com.dpc.smart_staffing_backend.entity.InterviewStatus;
import com.dpc.smart_staffing_backend.entity.StaffingRequest;
import com.dpc.smart_staffing_backend.exception.ResourceNotFoundException;
import com.dpc.smart_staffing_backend.mapper.ConsultantMapper;
import com.dpc.smart_staffing_backend.repository.ConsultantRepository;
import com.dpc.smart_staffing_backend.repository.InterviewRepository;
import com.dpc.smart_staffing_backend.repository.StaffingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ConsultantRepository consultantRepository;
    private final StaffingRequestRepository staffingRequestRepository;
    private final ConsultantMapper consultantMapper;

    public InterviewService(InterviewRepository interviewRepository,
                            ConsultantRepository consultantRepository,
                            StaffingRequestRepository staffingRequestRepository,
                            ConsultantMapper consultantMapper) {
        this.interviewRepository = interviewRepository;
        this.consultantRepository = consultantRepository;
        this.staffingRequestRepository = staffingRequestRepository;
        this.consultantMapper = consultantMapper;
    }

    public List<InterviewDTO> listAll() {
        return interviewRepository.findAll().stream()
                .sorted(Comparator.comparing(Interview::getDate).thenComparing(Interview::getTime))
                .map(this::toDTO)
                .toList();
    }

    // Consultants with no SCHEDULED interview — shown in the "Pending Requests" sidebar.
    public List<ConsultantResponseDTO> listPendingConsultants() {
        Set<Long> scheduledIds = interviewRepository.findByStatusOrderByDateAscTimeAsc(InterviewStatus.SCHEDULED)
                .stream()
                .map(i -> i.getConsultant().getId())
                .collect(Collectors.toSet());

        return consultantRepository.findAll().stream()
                .filter(c -> !scheduledIds.contains(c.getId()))
                .map(consultantMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public InterviewDTO create(InterviewRequestDTO dto) {
        Interview interview = new Interview(
                dto.date(),
                dto.time(),
                dto.location(),
                dto.notes(),
                resolveConsultant(dto.consultantId()),
                resolveRequest(dto.staffingRequestId())
        );
        if (dto.status() != null) {
            interview.setStatus(dto.status());
        }
        return toDTO(interviewRepository.save(interview));
    }

    @Transactional
    public InterviewDTO update(Long id, InterviewRequestDTO dto) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id " + id));

        interview.setDate(dto.date());
        interview.setTime(dto.time());
        interview.setLocation(dto.location());
        interview.setNotes(dto.notes());
        if (dto.status() != null) {
            interview.setStatus(dto.status());
        }
        interview.setConsultant(resolveConsultant(dto.consultantId()));
        interview.setStaffingRequest(resolveRequest(dto.staffingRequestId()));

        return toDTO(interviewRepository.save(interview));
    }

    @Transactional
    public InterviewDTO updateStatus(Long id, InterviewStatus status) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id " + id));
        interview.setStatus(status);
        return toDTO(interviewRepository.save(interview));
    }

    @Transactional
    public void delete(Long id) {
        if (!interviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Interview not found with id " + id);
        }
        interviewRepository.deleteById(id);
    }

    private Consultant resolveConsultant(Long consultantId) {
        return consultantRepository.findById(consultantId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found with id " + consultantId));
    }

    private StaffingRequest resolveRequest(Long requestId) {
        if (requestId == null) return null;
        return staffingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id " + requestId));
    }

    private InterviewDTO toDTO(Interview entity) {
        return new InterviewDTO(
                entity.getId(),
                entity.getDate(),
                entity.getTime(),
                entity.getLocation(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getConsultant().getId(),
                entity.getConsultant().getName(),
                entity.getStaffingRequest() != null ? entity.getStaffingRequest().getId() : null,
                entity.getStaffingRequest() != null ? entity.getStaffingRequest().getTitle() : null
        );
    }
}
