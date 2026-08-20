package com.dpc.smart_staffing_backend.config;

import com.dpc.smart_staffing_backend.entity.HRMember;
import com.dpc.smart_staffing_backend.repository.HRMemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Sprint 1 has no registration endpoint (there's only one role, and no admin to manage
// accounts yet), so this seeds a single HR member on startup — the only way to log in
// during development. Runs once: skipped once at least one HR member already exists.
@Component
public class DataSeeder implements CommandLineRunner {

    private final HRMemberRepository hrMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${hr.seed.email}")
    private String seedEmail;

    @Value("${hr.seed.password}")
    private String seedPassword;

    public DataSeeder(HRMemberRepository hrMemberRepository, PasswordEncoder passwordEncoder) {
        this.hrMemberRepository = hrMemberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (hrMemberRepository.count() == 0) {
            HRMember hrMember = new HRMember("Test HR Member", seedEmail, passwordEncoder.encode(seedPassword));
            hrMemberRepository.save(hrMember);
        }
    }
}
