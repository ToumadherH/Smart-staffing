package com.dpc.smart_staffing_backend.service;

import com.dpc.smart_staffing_backend.dto.ConsultantRequestDTO;
import com.dpc.smart_staffing_backend.dto.ConsultantResponseDTO;
import com.dpc.smart_staffing_backend.dto.SkillDTO;
import com.dpc.smart_staffing_backend.entity.Availability;
import com.dpc.smart_staffing_backend.entity.Skill;
import com.dpc.smart_staffing_backend.exception.EmailAlreadyExistsException;
import com.dpc.smart_staffing_backend.exception.ResourceNotFoundException;
import com.dpc.smart_staffing_backend.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @Transactional here wraps each test in a transaction that is rolled back afterwards,
// so these tests hit the real dev database without leaving any rows behind.
@SpringBootTest
@Transactional
class ConsultantServiceIntegrationTest {

    @Autowired
    private ConsultantService consultantService;

    @Autowired
    private SkillRepository skillRepository;

    @Test
    void createConsultant_reusesExistingSkillAndCreatesNewOne() {
        // The integration suite uses the local development database, which can already
        // contain Java from an earlier manual run. Reuse it so this test remains repeatable.
        Skill existingJava = skillRepository.findByNameIgnoreCase("Java")
                .orElseGet(() -> skillRepository.save(new Skill("Java", "Backend")));

        String uniqueEmail = "skill-test-" + UUID.randomUUID() + "@dpc.com";
        ConsultantRequestDTO dto = new ConsultantRequestDTO(
                "Jane Doe", uniqueEmail, "0600000000", 5,
                Availability.AVAILABLE, null, "Tunis",
                List.of("French", "English"),
                List.of(new SkillDTO(null, "java", "Backend"), new SkillDTO(null, "Kubernetes", "DevOps"))
        );

        ConsultantResponseDTO response = consultantService.createConsultant(dto);

        assertNotNull(response.id());
        assertEquals(2, response.skills().size());
        assertTrue(response.skills().stream().anyMatch(s -> s.id().equals(existingJava.getId())));
        // Case-insensitive match must reuse the existing row, not create "java" as a duplicate.
        assertTrue(skillRepository.findByNameIgnoreCase("java").isPresent());
        assertEquals(1, skillRepository.findAll().stream().filter(s -> s.getName().equalsIgnoreCase("java")).count());
    }

    @Test
    void createConsultant_duplicateEmail_throws() {
        String uniqueEmail = "duplicate-test-" + UUID.randomUUID() + "@dpc.com";
        ConsultantRequestDTO dto = new ConsultantRequestDTO(
                "Jane Doe", uniqueEmail, null, 3,
                Availability.AVAILABLE, null, "Tunis", null, null
        );
        consultantService.createConsultant(dto);

        assertThrows(EmailAlreadyExistsException.class, () -> consultantService.createConsultant(dto));
    }

    @Test
    void getConsultantById_notFound_throws() {
        assertThrows(ResourceNotFoundException.class, () -> consultantService.getConsultantById(Long.MAX_VALUE));
    }
}
