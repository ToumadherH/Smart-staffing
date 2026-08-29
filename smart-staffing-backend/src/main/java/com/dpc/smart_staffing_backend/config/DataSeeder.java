package com.dpc.smart_staffing_backend.config;

import com.dpc.smart_staffing_backend.entity.Availability;
import com.dpc.smart_staffing_backend.entity.Consultant;
import com.dpc.smart_staffing_backend.entity.HRMember;
import com.dpc.smart_staffing_backend.entity.Interview;
import com.dpc.smart_staffing_backend.entity.InterviewStatus;
import com.dpc.smart_staffing_backend.entity.Skill;
import com.dpc.smart_staffing_backend.entity.StaffingRequest;
import com.dpc.smart_staffing_backend.entity.StaffingRequestStatus;
import com.dpc.smart_staffing_backend.repository.ConsultantRepository;
import com.dpc.smart_staffing_backend.repository.HRMemberRepository;
import com.dpc.smart_staffing_backend.repository.InterviewRepository;
import com.dpc.smart_staffing_backend.repository.SkillRepository;
import com.dpc.smart_staffing_backend.repository.StaffingRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final HRMemberRepository hrMemberRepository;
    private final ConsultantRepository consultantRepository;
    private final SkillRepository skillRepository;
    private final StaffingRequestRepository staffingRequestRepository;
    private final InterviewRepository interviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${hr.seed.email:hr@dpc.com}")
    private String seedEmail;

    @Value("${hr.seed.password:ChangeMe123!}")
    private String seedPassword;

    public DataSeeder(HRMemberRepository hrMemberRepository,
                      ConsultantRepository consultantRepository,
                      SkillRepository skillRepository,
                      StaffingRequestRepository staffingRequestRepository,
                      InterviewRepository interviewRepository,
                      PasswordEncoder passwordEncoder) {
        this.hrMemberRepository = hrMemberRepository;
        this.consultantRepository = consultantRepository;
        this.skillRepository = skillRepository;
        this.staffingRequestRepository = staffingRequestRepository;
        this.interviewRepository = interviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Seed HR Member
        if (hrMemberRepository.count() == 0) {
            HRMember hrMember = new HRMember("Sarah Jenkins (HR)", seedEmail, passwordEncoder.encode(seedPassword));
            hrMemberRepository.save(hrMember);
        }

        // 2. Seed Consultants & Skills if few consultants exist
        if (consultantRepository.count() < 5) {
            Map<String, Skill> skillsMap = new HashMap<>();

            String[][] defaultSkills = {
                    {"React", "Frontend"},
                    {"TypeScript", "Language"},
                    {"Angular", "Frontend"},
                    {"Next.js", "Frontend"},
                    {"Redux", "Frontend"},
                    {"Vue", "Frontend"},
                    {"Tailwind CSS", "Frontend"},
                    {"CSS3 / HTML5", "Frontend"},
                    {"Node.js", "Backend"},
                    {"Java", "Backend"},
                    {"Spring Boot", "Backend"},
                    {"PostgreSQL", "Database"},
                    {"SQL", "Database"},
                    {"Python", "Data / Backend"},
                    {"Apache Spark", "Data Engineering"},
                    {"PySpark", "Data Engineering"},
                    {"Airflow", "Data / DevOps"},
                    {"Prefect", "Data / DevOps"},
                    {"AWS Redshift", "Cloud Data"},
                    {"Kubernetes", "DevOps / Cloud"},
                    {"AWS EKS", "Cloud"},
                    {"Terraform", "DevOps"},
                    {"Docker", "DevOps"},
                    {"CI/CD", "DevOps"},
                    {"Go (Golang)", "Backend"},
                    {"Prometheus", "DevOps"},
                    {"GCP", "Cloud"},
                    {"Kafka", "Backend"},
                    {"GraphQL", "Backend / API"},
                    {"REST APIs", "API"}
            };

            for (String[] def : defaultSkills) {
                Skill s = skillRepository.findByNameIgnoreCase(def[0])
                        .orElseGet(() -> skillRepository.save(new Skill(def[0], def[1])));
                skillsMap.put(def[0].toLowerCase(), s);
            }

            // Create Marcus Chen
            if (!consultantRepository.existsByEmail("marcus.chen@dpc.com")) {
                Consultant c1 = new Consultant(
                        "Marcus Chen", "marcus.chen@dpc.com", "+33 6 12 34 56 78", 8,
                        Availability.AVAILABLE, "Open for new assignments", "Paris, France (Remote)"
                );
                c1.setLanguages(List.of("English (Native)", "French (B2)"));
                c1.setSkills(nonNullSet(
                        skillsMap.get("react"), skillsMap.get("typescript"), skillsMap.get("angular"),
                        skillsMap.get("next.js"), skillsMap.get("redux"), skillsMap.get("docker"),
                        skillsMap.get("ci/cd"), skillsMap.get("rest apis")
                ));
                consultantRepository.save(c1);
            }

            // Create Elena Rodriguez
            if (!consultantRepository.existsByEmail("elena.rodriguez@dpc.com")) {
                Consultant c2 = new Consultant(
                        "Elena Rodriguez", "elena.rodriguez@dpc.com", "+49 30 9876543", 8,
                        Availability.AVAILABLE, "FinTech Cloud Migration Project", "Berlin, DE (Remote)"
                );
                c2.setLanguages(List.of("English (Native)", "Spanish (Fluent)", "German (B2)"));
                c2.setSkills(nonNullSet(
                        skillsMap.get("kubernetes"), skillsMap.get("aws eks"), skillsMap.get("terraform"),
                        skillsMap.get("python"), skillsMap.get("ci/cd"), skillsMap.get("go (golang)"),
                        skillsMap.get("prometheus"), skillsMap.get("docker")
                ));
                consultantRepository.save(c2);
            }

            // Create David Kim
            if (!consultantRepository.existsByEmail("david.kim@dpc.com")) {
                Consultant c3 = new Consultant(
                        "David Kim", "david.kim@dpc.com", "+1 415 555 0192", 10,
                        Availability.ASSIGNED, "Core Banking UI Modernization", "San Francisco, US (Remote)"
                );
                c3.setLanguages(List.of("English (Native)", "Korean (Fluent)"));
                c3.setSkills(nonNullSet(
                        skillsMap.get("react"), skillsMap.get("typescript"), skillsMap.get("vue"),
                        skillsMap.get("node.js"), skillsMap.get("rest apis")
                ));
                consultantRepository.save(c3);
            }

            // Create Aisha Johnson
            if (!consultantRepository.existsByEmail("aisha.johnson@dpc.com")) {
                Consultant c4 = new Consultant(
                        "Aisha Johnson", "aisha.johnson@dpc.com", "+44 20 7946 0912", 5,
                        Availability.AVAILABLE, "Available immediately", "London, UK"
                );
                c4.setLanguages(List.of("English (Native)"));
                c4.setSkills(nonNullSet(
                        skillsMap.get("react"), skillsMap.get("typescript"), skillsMap.get("graphql"),
                        skillsMap.get("css3 / html5"), skillsMap.get("tailwind css")
                ));
                consultantRepository.save(c4);
            }

            // Create Thomas Wright
            if (!consultantRepository.existsByEmail("thomas.wright@dpc.com")) {
                Consultant c5 = new Consultant(
                        "Thomas Wright", "thomas.wright@dpc.com", "+33 6 99 88 77 66", 4,
                        Availability.AVAILABLE, "Available immediately", "Lyon, France"
                );
                c5.setLanguages(List.of("French (Native)", "English (Fluent)"));
                c5.setSkills(nonNullSet(
                        skillsMap.get("react"), skillsMap.get("css3 / html5"), skillsMap.get("rest apis"),
                        skillsMap.get("typescript")
                ));
                consultantRepository.save(c5);
            }

            // Create Alex Chen
            if (!consultantRepository.existsByEmail("alex.chen@dpc.com")) {
                Consultant c6 = new Consultant(
                        "Alex Chen", "alex.chen@dpc.com", "+1 617 555 3344", 6,
                        Availability.AVAILABLE, "Big Data Pipeline Specialist", "Boston, US (Remote)"
                );
                c6.setLanguages(List.of("English (Native)", "Mandarin (Fluent)"));
                c6.setSkills(nonNullSet(
                        skillsMap.get("python"), skillsMap.get("apache spark"), skillsMap.get("pyspark"),
                        skillsMap.get("prefect"), skillsMap.get("sql"), skillsMap.get("postgresql")
                ));
                consultantRepository.save(c6);
            }

            // Create Alice Johnson
            if (!consultantRepository.existsByEmail("alice.johnson@dpc.com")) {
                Consultant c7 = new Consultant(
                        "Alice Johnson", "alice.johnson@dpc.com", "+33 1 44 55 66 77", 7,
                        Availability.AVAILABLE, "Technical Lead / Senior Architect", "Paris, France"
                );
                c7.setLanguages(List.of("English (Fluent)", "French (Native)"));
                c7.setSkills(nonNullSet(
                        skillsMap.get("java"), skillsMap.get("spring boot"), skillsMap.get("postgresql"),
                        skillsMap.get("docker"), skillsMap.get("kafka")
                ));
                consultantRepository.save(c7);
            }

            // Create Bob Williams
            if (!consultantRepository.existsByEmail("bob.williams@dpc.com")) {
                Consultant c8 = new Consultant(
                        "Bob Williams", "bob.williams@dpc.com", "+1 312 555 8899", 5,
                        Availability.AVAILABLE, "Design System Architect", "Chicago, US (Remote)"
                );
                c8.setLanguages(List.of("English (Native)"));
                c8.setSkills(nonNullSet(
                        skillsMap.get("react"), skillsMap.get("css3 / html5"), skillsMap.get("tailwind css"),
                        skillsMap.get("typescript")
                ));
                consultantRepository.save(c8);
            }
        }

        // 3. Seed Staffing Requests
        if (staffingRequestRepository.count() < 4) {
            Skill sReact = skillRepository.findByNameIgnoreCase("React").orElse(null);
            Skill sTs = skillRepository.findByNameIgnoreCase("TypeScript").orElse(null);
            Skill sRedux = skillRepository.findByNameIgnoreCase("Redux").orElse(null);
            Skill sPython = skillRepository.findByNameIgnoreCase("Python").orElse(null);
            Skill sSpark = skillRepository.findByNameIgnoreCase("Apache Spark").orElse(null);
            Skill sAirflow = skillRepository.findByNameIgnoreCase("Airflow").orElse(null);
            Skill sRedshift = skillRepository.findByNameIgnoreCase("AWS Redshift").orElse(null);
            Skill sK8s = skillRepository.findByNameIgnoreCase("Kubernetes").orElse(null);
            Skill sEks = skillRepository.findByNameIgnoreCase("AWS EKS").orElse(null);
            Skill sTerraform = skillRepository.findByNameIgnoreCase("Terraform").orElse(null);
            Skill sJava = skillRepository.findByNameIgnoreCase("Java").orElse(null);
            Skill sSpring = skillRepository.findByNameIgnoreCase("Spring Boot").orElse(null);
            Skill sPg = skillRepository.findByNameIgnoreCase("PostgreSQL").orElse(null);

            StaffingRequest r1 = new StaffingRequest(
                    "Senior Frontend Engineer",
                    "Acme Corp",
                    "Paris / Remote",
                    5,
                    "Lead frontend development for Acme Corp enterprise micro-frontend platform. Strong React & TypeScript expertise required.",
                    StaffingRequestStatus.OPEN
            );
            r1.setRequiredSkills(nonNullSet(sReact, sTs, sRedux));
            staffingRequestRepository.save(r1);

            StaffingRequest r2 = new StaffingRequest(
                    "Senior Data Engineer",
                    "DataScale Analytics",
                    "London / Remote",
                    5,
                    "Build large-scale real-time data ingestion pipelines using Spark, Airflow and Redshift.",
                    StaffingRequestStatus.OPEN
            );
            r2.setRequiredSkills(nonNullSet(sPython, sSpark, sAirflow, sRedshift));
            staffingRequestRepository.save(r2);

            StaffingRequest r3 = new StaffingRequest(
                    "Senior SRE & Cloud Architect",
                    "FinTech Innovators Ltd.",
                    "Berlin, DE (Remote)",
                    6,
                    "Lead cloud infrastructure scaling, multi-region Kubernetes clusters, and Terraform IaC security.",
                    StaffingRequestStatus.IN_PROGRESS
            );
            r3.setRequiredSkills(nonNullSet(sK8s, sEks, sTerraform, sPython));
            staffingRequestRepository.save(r3);

            StaffingRequest r4 = new StaffingRequest(
                    "Full Stack Java / Angular Engineer",
                    "Global Retail Systems",
                    "Tunis / Hybrid",
                    4,
                    "Develop and maintain high-throughput retail checkout APIs using Spring Boot and Angular.",
                    StaffingRequestStatus.OPEN
            );
            r4.setRequiredSkills(nonNullSet(sJava, sSpring, sPg));
            staffingRequestRepository.save(r4);
        }

        // 4. Seed Interviews
        if (interviewRepository.count() == 0) {
            Consultant marcus = consultantRepository.findByEmail("marcus.chen@dpc.com").orElse(null);
            Consultant elena = consultantRepository.findByEmail("elena.rodriguez@dpc.com").orElse(null);
            Consultant alex = consultantRepository.findByEmail("alex.chen@dpc.com").orElse(null);
            Consultant david = consultantRepository.findByEmail("david.kim@dpc.com").orElse(null);

            StaffingRequest reqFrontend = staffingRequestRepository.findAll().stream()
                    .filter(r -> r.getTitle().contains("Frontend"))
                    .findFirst().orElse(null);

            StaffingRequest reqData = staffingRequestRepository.findAll().stream()
                    .filter(r -> r.getTitle().contains("Data"))
                    .findFirst().orElse(null);

            StaffingRequest reqSre = staffingRequestRepository.findAll().stream()
                    .filter(r -> r.getTitle().contains("SRE"))
                    .findFirst().orElse(null);

            LocalDate today = LocalDate.now();

            if (marcus != null) {
                Interview i1 = new Interview(today.plusDays(1), "10:00", "Google Meet", "Technical screening for React Lead role", marcus, reqFrontend);
                interviewRepository.save(i1);
            }
            if (elena != null) {
                Interview i2 = new Interview(today.plusDays(3), "14:00", "Zoom", "System Architecture deep-dive panel", elena, reqSre);
                interviewRepository.save(i2);
            }
            if (alex != null) {
                Interview i3 = new Interview(today.plusDays(5), "11:30", "Teams", "Data engineering pipeline design interview", alex, reqData);
                interviewRepository.save(i3);
            }
            if (david != null) {
                Interview i4 = new Interview(today.plusDays(7), "15:00", "Google Meet", "Client culture & technical fit", david, reqFrontend);
                i4.setStatus(InterviewStatus.SCHEDULED);
                interviewRepository.save(i4);
            }
        }
    }

    private Set<Skill> nonNullSet(Skill... skills) {
        Set<Skill> set = new HashSet<>();
        for (Skill s : skills) {
            if (s != null) set.add(s);
        }
        return set;
    }
}
