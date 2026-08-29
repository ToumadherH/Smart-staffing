package com.dpc.smart_staffing_backend.service;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CvExtractionTest {

    private final CvExtractionService cvExtractionService = new CvExtractionService();

    @Test
    void testExtractTextAndSkills() {
        String cvText = """
                John Doe
                Senior Software Engineer
                Email: john.doe@example.com
                Phone: +216-98-123-456
                Skills: Java, Spring Boot, Angular, PostgreSQL, Docker, Microservices
                Experience in REST API development and Agile methodology.
                """;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(cvText.getBytes(StandardCharsets.UTF_8));

        CvExtractionService.ExtractionResult result = cvExtractionService.extract(inputStream, "sample_cv.txt");

        assertNotNull(result.extractedText());
        assertTrue(result.extractedText().contains("Senior Software Engineer"));
        assertEquals("john.doe@example.com", result.extractedEmail());
        assertTrue(result.extractedSkills().contains("Java"));
        assertTrue(result.extractedSkills().contains("Spring Boot"));
        assertTrue(result.extractedSkills().contains("Angular"));
    }
}
