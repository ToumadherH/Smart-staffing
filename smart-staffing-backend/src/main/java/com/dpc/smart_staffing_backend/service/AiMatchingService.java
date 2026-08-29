package com.dpc.smart_staffing_backend.service;

import com.dpc.smart_staffing_backend.dto.ConsultantMatchDTO;
import com.dpc.smart_staffing_backend.dto.ConsultantResponseDTO;
import com.dpc.smart_staffing_backend.entity.Availability;
import com.dpc.smart_staffing_backend.entity.Consultant;
import com.dpc.smart_staffing_backend.entity.Skill;
import com.dpc.smart_staffing_backend.entity.StaffingRequest;
import com.dpc.smart_staffing_backend.exception.ResourceNotFoundException;
import com.dpc.smart_staffing_backend.mapper.ConsultantMapper;
import com.dpc.smart_staffing_backend.repository.ConsultantRepository;
import com.dpc.smart_staffing_backend.repository.StaffingRequestRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AiMatchingService {

    private static final Logger log = LoggerFactory.getLogger(AiMatchingService.class);

    private final StaffingRequestRepository staffingRequestRepository;
    private final ConsultantRepository consultantRepository;
    private final ConsultantMapper consultantMapper;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:${OPENAI_API_KEY:}}")
    private String apiKey;

    public AiMatchingService(StaffingRequestRepository staffingRequestRepository,
                             ConsultantRepository consultantRepository,
                             ConsultantMapper consultantMapper) {
        this.staffingRequestRepository = staffingRequestRepository;
        this.consultantRepository = consultantRepository;
        this.consultantMapper = consultantMapper;
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    }

    public List<ConsultantMatchDTO> findMatchesForRequest(Long requestId) {
        StaffingRequest request = staffingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id " + requestId));

        List<Consultant> consultants = consultantRepository.findAll();
        List<ConsultantMatchDTO> matches = new ArrayList<>();

        String effectiveKey = getEffectiveApiKey();

        for (Consultant consultant : consultants) {
            ConsultantMatchDTO match;
            if (effectiveKey != null && !effectiveKey.isBlank()) {
                match = matchWithOpenAi(request, consultant, effectiveKey);
            } else {
                match = matchWithRuleBased(request, consultant);
            }
            matches.add(match);
        }

        matches.sort(Comparator.comparing(ConsultantMatchDTO::matchScore).reversed());
        return matches;
    }

    private String getEffectiveApiKey() {
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey.trim();
        }
        String envKey = System.getenv("OPENAI_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }
        return null;
    }

    private ConsultantMatchDTO matchWithOpenAi(StaffingRequest request, Consultant consultant, String key) {
        try {
            String reqSkillsStr = request.getRequiredSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.joining(", "));
            String consultantSkillsStr = consultant.getSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.joining(", "));
            String cvText = (consultant.getCv() != null && consultant.getCv().getExtractedText() != null)
                    ? consultant.getCv().getExtractedText()
                    : "No CV text available";

            if (cvText.length() > 3000) {
                cvText = cvText.substring(0, 3000) + "... [truncated]";
            }

            String userPrompt = String.format("""
                    STAFFING REQUEST REQUIREMENTS:
                    - Title: %s
                    - Client: %s
                    - Location: %s
                    - Years of Experience Required: %s
                    - Required Skills: %s
                    - Description: %s

                    CONSULTANT CANDIDATE:
                    - Name: %s
                    - Years of Experience: %d
                    - Availability: %s
                    - Explicit Skills: %s
                    - CV Extracted Text: %s
                    """,
                    request.getTitle(),
                    request.getClientName(),
                    request.getLocation() != null ? request.getLocation() : "N/A",
                    request.getYearsOfExperienceRequired() != null ? request.getYearsOfExperienceRequired() + " years" : "N/A",
                    reqSkillsStr.isEmpty() ? "None specified" : reqSkillsStr,
                    request.getDescription() != null ? request.getDescription() : "N/A",
                    consultant.getName(),
                    consultant.getYearsOfExperience(),
                    consultant.getAvailability(),
                    consultantSkillsStr.isEmpty() ? "None listed" : consultantSkillsStr,
                    cvText
            );

            Map<String, Object> systemMessage = Map.of(
                    "role", "system",
                    "content", "You are an AI talent matching assistant. Analyze candidate CV text, skills, and experience against a staffing request's requirements. Use semantic matching (e.g., Containerization matches Docker, Java backend matches Spring Boot, Relational DB matches PostgreSQL). Output ONLY a valid JSON object with keys: 'matchingScore' (number 0-100), 'matchedSkills' (array of string skill names), 'missingSkills' (array of string skill names), and 'matchReason' (string explanation). Do not wrap in markdown syntax or code blocks."
            );

            Map<String, Object> userMessage = Map.of(
                    "role", "user",
                    "content", userPrompt
            );

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(systemMessage, userMessage),
                    "temperature", 0.2,
                    "response_format", Map.of("type", "json_object")
            );

            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + key)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            JsonNode matchJson = objectMapper.readTree(content);
            double score = matchJson.path("matchingScore").asDouble(0.0);

            List<String> matchedSkills = new ArrayList<>();
            if (matchJson.has("matchedSkills") && matchJson.get("matchedSkills").isArray()) {
                matchJson.get("matchedSkills").forEach(n -> matchedSkills.add(n.asText()));
            }

            List<String> missingSkills = new ArrayList<>();
            if (matchJson.has("missingSkills") && matchJson.get("missingSkills").isArray()) {
                matchJson.get("missingSkills").forEach(n -> missingSkills.add(n.asText()));
            }

            String matchReason = matchJson.path("matchReason").asText("OpenAI semantic evaluation complete.");

            ConsultantResponseDTO consultantDTO = consultantMapper.toResponseDTO(consultant);
            return new ConsultantMatchDTO(consultantDTO, score, matchedSkills, missingSkills, matchReason);

        } catch (Exception e) {
            log.warn("OpenAI API call failed for consultant {}: {}. Falling back to rule-based evaluation.", consultant.getId(), e.getMessage());
            return matchWithRuleBased(request, consultant);
        }
    }

    private ConsultantMatchDTO matchWithRuleBased(StaffingRequest request, Consultant consultant) {
        Set<String> reqSkillNames = request.getRequiredSkills().stream()
                .map(s -> s.getName().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> consultantSkillNames = consultant.getSkills().stream()
                .map(s -> s.getName().toLowerCase())
                .collect(Collectors.toSet());

        String cvTextLower = (consultant.getCv() != null && consultant.getCv().getExtractedText() != null)
                ? consultant.getCv().getExtractedText().toLowerCase()
                : "";

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (Skill reqSkill : request.getRequiredSkills()) {
            String nameLower = reqSkill.getName().toLowerCase();
            boolean isMatched = consultantSkillNames.contains(nameLower)
                    || (!cvTextLower.isEmpty() && cvTextLower.contains(nameLower))
            || isSemanticMatch(nameLower, consultantSkillNames, cvTextLower);

            if (isMatched) {
                matchedSkills.add(reqSkill.getName());
            } else {
                missingSkills.add(reqSkill.getName());
            }
        }

        // Skill score (weight: 60%)
        double skillScore = reqSkillNames.isEmpty() ? 60.0 : ((double) matchedSkills.size() / reqSkillNames.size()) * 60.0;

        // Experience score (weight: 20%)
        double expScore = 20.0;
        if (request.getYearsOfExperienceRequired() != null && request.getYearsOfExperienceRequired() > 0) {
            int reqExp = request.getYearsOfExperienceRequired();
            int actualExp = consultant.getYearsOfExperience();
            expScore = actualExp >= reqExp ? 20.0 : ((double) actualExp / reqExp) * 20.0;
        }

        // Availability score (weight: 20%)
        double availScore = 20.0;
        if (consultant.getAvailability() == Availability.ASSIGNED) {
            availScore = 10.0;
        } else if (consultant.getAvailability() == Availability.ON_LEAVE) {
            availScore = 5.0;
        }

        double totalScore = Math.round((skillScore + expScore + availScore) * 10.0) / 10.0;
        totalScore = Math.min(100.0, totalScore);

        String reason = String.format("Matched %d/%d required skills. %d yrs exp (Req: %s). Status: %s.",
                matchedSkills.size(),
                reqSkillNames.size(),
                consultant.getYearsOfExperience(),
                request.getYearsOfExperienceRequired() != null ? request.getYearsOfExperienceRequired() + " yrs" : "N/A",
                consultant.getAvailability()
        );

        ConsultantResponseDTO consultantDTO = consultantMapper.toResponseDTO(consultant);
        return new ConsultantMatchDTO(consultantDTO, totalScore, matchedSkills, missingSkills, reason);
    }

    private boolean isSemanticMatch(String reqSkillLower, Set<String> consultantSkillsLower, String cvTextLower) {
        if (reqSkillLower.contains("container") || reqSkillLower.contains("docker")) {
            return consultantSkillsLower.contains("docker") || consultantSkillsLower.contains("kubernetes") || cvTextLower.contains("docker");
        }
        if (reqSkillLower.contains("java") || reqSkillLower.contains("spring")) {
            return consultantSkillsLower.contains("java") || consultantSkillsLower.contains("spring boot") || cvTextLower.contains("java");
        }
        if (reqSkillLower.contains("relational") || reqSkillLower.contains("sql") || reqSkillLower.contains("database")) {
            return consultantSkillsLower.contains("postgresql") || consultantSkillsLower.contains("mysql") || consultantSkillsLower.contains("sql") || cvTextLower.contains("postgres");
        }
        return false;
    }
}
