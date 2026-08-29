package com.dpc.smart_staffing_backend.dto;

import com.dpc.smart_staffing_backend.entity.InterviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InterviewRequestDTO(
        @NotNull(message = "Date is required")
        LocalDate date,

        @NotBlank(message = "Time is required")
        String time,

        String location,
        InterviewStatus status,
        String notes,

        @NotNull(message = "Consultant is required")
        Long consultantId,

        Long staffingRequestId
) {}
