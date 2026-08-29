package com.dpc.smart_staffing_backend.controller;

import com.dpc.smart_staffing_backend.dto.ConsultantResponseDTO;
import com.dpc.smart_staffing_backend.dto.InterviewDTO;
import com.dpc.smart_staffing_backend.dto.InterviewRequestDTO;
import com.dpc.smart_staffing_backend.entity.InterviewStatus;
import com.dpc.smart_staffing_backend.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    public List<InterviewDTO> listAll() {
        return interviewService.listAll();
    }

    @GetMapping("/pending-consultants")
    public List<ConsultantResponseDTO> listPendingConsultants() {
        return interviewService.listPendingConsultants();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewDTO create(@Valid @RequestBody InterviewRequestDTO dto) {
        return interviewService.create(dto);
    }

    @PutMapping("/{id}")
    public InterviewDTO update(@PathVariable Long id, @Valid @RequestBody InterviewRequestDTO dto) {
        return interviewService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    public InterviewDTO updateStatus(@PathVariable Long id, @RequestParam InterviewStatus status) {
        return interviewService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
