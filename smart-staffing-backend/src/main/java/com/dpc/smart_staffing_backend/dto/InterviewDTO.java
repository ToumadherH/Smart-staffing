package com.dpc.smart_staffing_backend.dto;

import com.dpc.smart_staffing_backend.entity.InterviewStatus;

import java.time.LocalDate;

public record InterviewDTO(
        Long id,
        LocalDate date,
        String time,
        String location,
        InterviewStatus status,
        String notes,
        Long consultantId,
        String consultantName,
        Long staffingRequestId,
        String staffingRequestTitle
) {}
