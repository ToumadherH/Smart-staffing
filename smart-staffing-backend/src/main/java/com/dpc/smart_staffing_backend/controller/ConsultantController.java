package com.dpc.smart_staffing_backend.controller;

import com.dpc.smart_staffing_backend.dto.ConsultantRequestDTO;
import com.dpc.smart_staffing_backend.dto.ConsultantResponseDTO;
import com.dpc.smart_staffing_backend.dto.CvResponseDTO;
import com.dpc.smart_staffing_backend.service.ConsultantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Thin by design: every method just delegates to ConsultantService.
// @Valid triggers Bean Validation on the request body before the method body even runs.
@RestController
@RequestMapping("/api/consultants")
public class ConsultantController {

    private final ConsultantService consultantService;

    public ConsultantController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    @GetMapping
    public List<ConsultantResponseDTO> getAllConsultants() {
        return consultantService.getAllConsultants();
    }

    @GetMapping("/{id}")
    public ConsultantResponseDTO getConsultantById(@PathVariable Long id) {
        return consultantService.getConsultantById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultantResponseDTO createConsultant(@Valid @RequestBody ConsultantRequestDTO request) {
        return consultantService.createConsultant(request);
    }

    @PutMapping("/{id}")
    public ConsultantResponseDTO updateConsultant(@PathVariable Long id,
                                                    @Valid @RequestBody ConsultantRequestDTO request) {
        return consultantService.updateConsultant(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConsultant(@PathVariable Long id) {
        consultantService.deleteConsultant(id);
    }

    @PostMapping(path = "/{id}/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CvResponseDTO uploadCv(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return consultantService.uploadCv(id, file);
    }

    @GetMapping("/{id}/cv/download")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long id) {
        ConsultantService.CvFile cv = consultantService.downloadCv(id);
        MediaType mediaType = MediaType.parseMediaType(cv.contentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Content-Disposition", ContentDisposition.attachment().filename(cv.fileName()).build().toString())
                .body(cv.resource());
    }
}
