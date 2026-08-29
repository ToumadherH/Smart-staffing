package com.dpc.smart_staffing_backend.service;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CvExtractionService {

    private static final Logger log = LoggerFactory.getLogger(CvExtractionService.class);
    private final Tika tika = new Tika();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?[0-9]{1,3}[- .]?\\(?[0-9]{2,3}\\)?[- .]?[0-9]{3,4}[- .]?[0-9]{3,4}");

    private static final List<String> KNOWN_SKILLS = Arrays.asList(
            "Java", "Spring Boot", "Spring", "Angular", "React", "TypeScript", "JavaScript",
            "Node.js", "Python", "Django", "Flask", "Docker", "Kubernetes", "AWS", "Azure",
            "PostgreSQL", "MySQL", "SQL", "MongoDB", "Redis", "Git", "DevOps", "Microservices",
            "REST API", "Scrum", "Agile", "CI/CD", "Linux", "SCSS", "CSS", "HTML", "Hibernate",
            "JPA", "Maven", "Gradle", "C++", "C#", ".NET", "Go", "Golang", "PHP", "Vue.js", "GraphQL"
    );

    public record ExtractionResult(
            String extractedText,
            String extractedEmail,
            String extractedPhone,
            Set<String> extractedSkills
    ) {}

    public ExtractionResult extract(InputStream inputStream, String fileName) {
        String text = "";
        try {
            text = tika.parseToString(inputStream);
        } catch (Exception e) {
            log.warn("Could not extract text from CV file {}: {}", fileName, e.getMessage(), e);
        }

        String email = findPattern(text, EMAIL_PATTERN);
        String phone = findPattern(text, PHONE_PATTERN);
        Set<String> skills = extractSkillsFromText(text);

        return new ExtractionResult(
                text.isBlank() ? null : text,
                email,
                phone,
                skills
        );
    }

    private String findPattern(String text, Pattern pattern) {
        if (text == null || text.isBlank()) return null;
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private Set<String> extractSkillsFromText(String text) {
        Set<String> found = new HashSet<>();
        if (text == null || text.isBlank()) return found;

        String textLower = text.toLowerCase();
        for (String skill : KNOWN_SKILLS) {
            String skillLower = skill.toLowerCase();
            // Word boundary match
            String regex = "(?i)\\b" + Pattern.quote(skillLower) + "\\b";
            if (Pattern.compile(regex).matcher(textLower).find()) {
                found.add(skill);
            }
        }
        return found;
    }
}
