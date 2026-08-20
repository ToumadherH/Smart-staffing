package com.dpc.smart_staffing_backend.mapper;

import com.dpc.smart_staffing_backend.dto.ConsultantRequestDTO;
import com.dpc.smart_staffing_backend.dto.ConsultantResponseDTO;
import com.dpc.smart_staffing_backend.dto.CvResponseDTO;
import com.dpc.smart_staffing_backend.dto.SkillDTO;
import com.dpc.smart_staffing_backend.entity.Consultant;
import com.dpc.smart_staffing_backend.entity.Skill;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Pure field mapping only: no repository, no database access.
// Resolving skill names to existing/new Skill entities is a business decision
// (find-or-create), so it belongs in ConsultantService, not here.
@Component
public class ConsultantMapper {

    public Consultant toEntity(ConsultantRequestDTO dto) {
        Consultant consultant = new Consultant(
                dto.name(),
                dto.email(),
                dto.phone(),
                dto.yearsOfExperience(),
                dto.availability(),
                dto.currentMission(),
                dto.location()
        );
        consultant.setLanguages(copyOrEmpty(dto.languages()));
        return consultant;
    }

    public void updateEntity(Consultant consultant, ConsultantRequestDTO dto) {
        consultant.setName(dto.name());
        consultant.setEmail(dto.email());
        consultant.setPhone(dto.phone());
        consultant.setYearsOfExperience(dto.yearsOfExperience());
        consultant.setAvailability(dto.availability());
        consultant.setCurrentMission(dto.currentMission());
        consultant.setLocation(dto.location());
        consultant.setLanguages(copyOrEmpty(dto.languages()));
    }

    public ConsultantResponseDTO toResponseDTO(Consultant consultant) {
        List<SkillDTO> skills = consultant.getSkills().stream()
                .map(this::toSkillDTO)
                .sorted(Comparator.comparing(SkillDTO::name))
                .toList();

        return new ConsultantResponseDTO(
            consultant.getId(),
            consultant.getName(),
            consultant.getEmail(),
            consultant.getPhone(),
            consultant.getYearsOfExperience(),
            consultant.getAvailability(),
            consultant.getCurrentMission(),
            consultant.getLocation(),
            copyOrEmpty(consultant.getLanguages()),
            skills,
            consultant.getCv() == null ? null : toCvResponseDTO(consultant)
        );
    }

    private CvResponseDTO toCvResponseDTO(Consultant consultant) {
        var cv = consultant.getCv();
        return new CvResponseDTO(cv.getId(), cv.getFileName(), cv.getContentType(), cv.getUploadedAt(),
                "/api/consultants/" + consultant.getId() + "/cv/download");
    }

    private SkillDTO toSkillDTO(Skill skill) {
        return new SkillDTO(skill.getId(), skill.getName(), skill.getCategory());
    }

    private List<String> copyOrEmpty(List<String> values) {
        return values != null ? new ArrayList<>(values) : new ArrayList<>();
    }
}
