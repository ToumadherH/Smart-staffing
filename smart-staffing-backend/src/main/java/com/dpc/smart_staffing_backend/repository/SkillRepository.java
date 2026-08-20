package com.dpc.smart_staffing_backend.repository;

import com.dpc.smart_staffing_backend.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    // Used by ConsultantService's find-or-create logic: reuse an existing skill by
    // name (case-insensitive) instead of creating a duplicate ("Java" vs "java").
    Optional<Skill> findByNameIgnoreCase(String name);
}
