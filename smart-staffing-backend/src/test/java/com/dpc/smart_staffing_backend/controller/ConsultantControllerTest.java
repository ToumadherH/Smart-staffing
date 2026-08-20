package com.dpc.smart_staffing_backend.controller;

import com.dpc.smart_staffing_backend.dto.ConsultantResponseDTO;
import com.dpc.smart_staffing_backend.entity.Availability;
import com.dpc.smart_staffing_backend.exception.EmailAlreadyExistsException;
import com.dpc.smart_staffing_backend.exception.ResourceNotFoundException;
import com.dpc.smart_staffing_backend.service.ConsultantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest loads only the web layer (this controller + Spring MVC infrastructure),
// not the database or the rest of the application. ConsultantService is mocked, so this
// test proves routing, request validation, and status codes — not business logic
// (already covered separately by ConsultantServiceIntegrationTest).
//
// addFilters = false skips the Spring Security filter chain for this slice: securing
// endpoints is Step H's job and gets its own dedicated tests then.
@WebMvcTest(ConsultantController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsultantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultantService consultantService;

    private ConsultantResponseDTO sampleResponse(Long id) {
        return new ConsultantResponseDTO(id, "Jane Doe", "jane.doe@dpc.com", "0600000000", 5,
                Availability.AVAILABLE, null, "Tunis", List.of("French"), List.of(), null);
    }

    @Test
    void getAllConsultants_returnsOkWithList() throws Exception {
        when(consultantService.getAllConsultants()).thenReturn(List.of(sampleResponse(1L)));

        mockMvc.perform(get("/api/consultants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"));
    }

    @Test
    void getConsultantById_returnsOkWithConsultant() throws Exception {
        when(consultantService.getConsultantById(1L)).thenReturn(sampleResponse(1L));

        mockMvc.perform(get("/api/consultants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane.doe@dpc.com"));
    }

    @Test
    void createConsultant_validBody_returnsCreated() throws Exception {
        when(consultantService.createConsultant(any())).thenReturn(sampleResponse(1L));

        String validBody = """
                {
                  "name": "Jane Doe",
                  "email": "jane.doe@dpc.com",
                  "yearsOfExperience": 5,
                  "availability": "AVAILABLE",
                  "location": "Tunis"
                }
                """;

        mockMvc.perform(post("/api/consultants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createConsultant_invalidBody_returnsBadRequestAndNeverCallsService() throws Exception {
        // blank name, malformed email, negative years of experience
        String invalidBody = """
                {
                  "name": "",
                  "email": "not-an-email",
                  "yearsOfExperience": -1,
                  "availability": "AVAILABLE"
                }
                """;

        mockMvc.perform(post("/api/consultants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());

        verify(consultantService, never()).createConsultant(any());
    }

    @Test
    void updateConsultant_validBody_returnsOk() throws Exception {
        when(consultantService.updateConsultant(eq(1L), any())).thenReturn(sampleResponse(1L));

        String validBody = """
                {
                  "name": "Jane Doe",
                  "email": "jane.doe@dpc.com",
                  "yearsOfExperience": 6,
                  "availability": "ASSIGNED",
                  "location": "Tunis"
                }
                """;

        mockMvc.perform(put("/api/consultants/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isOk());
    }

    @Test
    void deleteConsultant_callsServiceAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/consultants/1"))
                .andExpect(status().isNoContent());

        verify(consultantService).deleteConsultant(1L);
    }

    @Test
    void uploadCv_withMultipartFile_returnsCreated() throws Exception {
        when(consultantService.uploadCv(eq(1L), any())).thenReturn(
                new com.dpc.smart_staffing_backend.dto.CvResponseDTO(
                        3L, "jane-cv.pdf", "application/pdf", java.time.Instant.now(),
                        "/api/consultants/1/cv/download")
        );
        MockMultipartFile file = new MockMultipartFile("file", "jane-cv.pdf", "application/pdf", "pdf".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/consultants/1/cv")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("jane-cv.pdf"));
    }

    // The next two tests prove the GlobalExceptionHandler (Step G) maps service-layer
    // exceptions to clean, correct HTTP responses instead of a raw 500.

    @Test
    void getConsultantById_unknownId_returns404WithCleanBody() throws Exception {
        when(consultantService.getConsultantById(99L))
                .thenThrow(new ResourceNotFoundException("Consultant not found with id 99"));

        mockMvc.perform(get("/api/consultants/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Consultant not found with id 99"));
    }

    @Test
    void createConsultant_duplicateEmail_returns409WithCleanBody() throws Exception {
        when(consultantService.createConsultant(any()))
                .thenThrow(new EmailAlreadyExistsException("A consultant with email jane.doe@dpc.com already exists"));

        String body = """
                {
                  "name": "Jane Doe",
                  "email": "jane.doe@dpc.com",
                  "yearsOfExperience": 5,
                  "availability": "AVAILABLE"
                }
                """;

        mockMvc.perform(post("/api/consultants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
