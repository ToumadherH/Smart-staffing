package com.dpc.smart_staffing_backend.controller;

import com.dpc.smart_staffing_backend.dto.ConsultantMatchDTO;
import com.dpc.smart_staffing_backend.dto.StaffingRequestDTO;
import com.dpc.smart_staffing_backend.dto.StaffingRequestRequestDTO;
import com.dpc.smart_staffing_backend.service.AiMatchingService;
import com.dpc.smart_staffing_backend.service.StaffingRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staffing-requests")
public class StaffingRequestController {

    private final StaffingRequestService staffingRequestService;
    private final AiMatchingService aiMatchingService;

    public StaffingRequestController(StaffingRequestService staffingRequestService,
                                   AiMatchingService aiMatchingService) {
        this.staffingRequestService = staffingRequestService;
        this.aiMatchingService = aiMatchingService;
    }

    @GetMapping
    public List<StaffingRequestDTO> listAll() {
        return staffingRequestService.listAll();
    }

    @GetMapping("/{id}")
    public StaffingRequestDTO getById(@PathVariable Long id) {
        return staffingRequestService.getById(id);
    }

    @PostMapping
    public ResponseEntity<StaffingRequestDTO> create(@Valid @RequestBody StaffingRequestRequestDTO requestDTO) {
        StaffingRequestDTO created = staffingRequestService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public StaffingRequestDTO update(@PathVariable Long id, @Valid @RequestBody StaffingRequestRequestDTO requestDTO) {
        return staffingRequestService.update(id, requestDTO);
    }

    @GetMapping("/{id}/matches")
    public List<ConsultantMatchDTO> getMatches(@PathVariable Long id) {
        return aiMatchingService.findMatchesForRequest(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        staffingRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
