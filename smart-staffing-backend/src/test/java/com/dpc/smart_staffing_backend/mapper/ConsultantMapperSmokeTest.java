package com.dpc.smart_staffing_backend.mapper;

import com.dpc.smart_staffing_backend.dto.ConsultantRequestDTO;
import com.dpc.smart_staffing_backend.dto.ConsultantResponseDTO;
import com.dpc.smart_staffing_backend.entity.Availability;
import com.dpc.smart_staffing_backend.entity.Consultant;
import com.dpc.smart_staffing_backend.entity.Skill;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsultantMapperSmokeTest {

    private final ConsultantMapper mapper = new ConsultantMapper();

    @Test
    void toEntity_mapsAllFieldsAndDefaultsNullListsToEmpty() {
        ConsultantRequestDTO dto = new ConsultantRequestDTO(
                "Jane Doe", "jane@dpc.com", "0600000000", 5,
                Availability.AVAILABLE, "Client X", "Tunis", null, null
        );

        Consultant consultant = mapper.toEntity(dto);

        assertEquals("Jane Doe", consultant.getName());
        assertEquals("jane@dpc.com", consultant.getEmail());
        assertEquals(Availability.AVAILABLE, consultant.getAvailability());
        assertTrue(consultant.getLanguages().isEmpty());
    }

    @Test
    void toResponseDTO_sortsSkillsAlphabeticallyByName() {
        Consultant consultant = new Consultant(
                "Jane Doe", "jane@dpc.com", "0600000000", 5,
                Availability.AVAILABLE, null, "Tunis"
        );
        consultant.setSkills(Set.of(new Skill("Spring", "Backend"), new Skill("Angular", "Frontend")));

        ConsultantResponseDTO response = mapper.toResponseDTO(consultant);

        assertEquals(List.of("Angular", "Spring"),
                response.skills().stream().map(s -> s.name()).toList());
    }
}
