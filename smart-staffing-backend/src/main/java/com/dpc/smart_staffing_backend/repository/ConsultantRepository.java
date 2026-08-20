package com.dpc.smart_staffing_backend.repository;

import com.dpc.smart_staffing_backend.entity.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultantRepository extends JpaRepository<Consultant, Long> {

    // Spring Data derives the SQL from the method name: no implementation needed.
    boolean existsByEmail(String email);
}
