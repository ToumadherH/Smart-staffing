package com.dpc.smart_staffing_backend.repository;

import com.dpc.smart_staffing_backend.entity.Cv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CvRepository extends JpaRepository<Cv, Long> {
    Optional<Cv> findByConsultantId(Long consultantId);
}
