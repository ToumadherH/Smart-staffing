package com.dpc.smart_staffing_backend.repository;

import com.dpc.smart_staffing_backend.entity.Availability;
import com.dpc.smart_staffing_backend.entity.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultantRepository extends JpaRepository<Consultant, Long> {

    boolean existsByEmail(String email);

    java.util.Optional<Consultant> findByEmail(String email);

    long countByAvailability(Availability availability);
}
