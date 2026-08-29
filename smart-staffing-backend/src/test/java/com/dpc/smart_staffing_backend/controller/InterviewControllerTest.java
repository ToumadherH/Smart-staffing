package com.dpc.smart_staffing_backend.controller;

import com.dpc.smart_staffing_backend.dto.InterviewDTO;
import com.dpc.smart_staffing_backend.dto.InterviewRequestDTO;
import com.dpc.smart_staffing_backend.entity.InterviewStatus;
import com.dpc.smart_staffing_backend.service.InterviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterviewService interviewService;

    private InterviewDTO sampleDTO() {
        return new InterviewDTO(
                1L, LocalDate.of(2026, 10, 5), "10:00", "Google Meet",
                InterviewStatus.SCHEDULED, "Screening", 12L, "Jane Doe", 1L, "Senior Engineer"
        );
    }

    @Test
    void listAll_returnsOk() throws Exception {
        when(interviewService.listAll()).thenReturn(List.of(sampleDTO()));

        mockMvc.perform(get("/api/interviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].consultantName").value("Jane Doe"));
    }

    @Test
    void create_returnsCreated() throws Exception {
        when(interviewService.create(any(InterviewRequestDTO.class))).thenReturn(sampleDTO());

        String json = """
                {
                  "date": "2026-10-05",
                  "time": "10:00",
                  "location": "Google Meet",
                  "status": "SCHEDULED",
                  "notes": "Screening",
                  "consultantId": 12,
                  "staffingRequestId": 1
                }
                """;

        mockMvc.perform(post("/api/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateStatus_returnsOk() throws Exception {
        when(interviewService.updateStatus(eq(1L), eq(InterviewStatus.COMPLETED))).thenReturn(
                new InterviewDTO(1L, LocalDate.of(2026, 10, 5), "10:00", "Google Meet",
                        InterviewStatus.COMPLETED, "Screening", 12L, "Jane Doe", 1L, "Senior Engineer")
        );

        mockMvc.perform(patch("/api/interviews/1/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/interviews/1"))
                .andExpect(status().isNoContent());

        verify(interviewService).delete(1L);
    }
}
