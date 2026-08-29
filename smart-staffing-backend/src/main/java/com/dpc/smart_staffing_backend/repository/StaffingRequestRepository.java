package com.dpc.smart_staffing_backend.repository;

import com.dpc.smart_staffing_backend.entity.StaffingRequest;
import com.dpc.smart_staffing_backend.entity.StaffingRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffingRequestRepository extends JpaRepository<StaffingRequest, Long> {
    long countByStatusNot(StaffingRequestStatus status);

    @EntityGraph(attributePaths = {"requiredSkills"})
    List<StaffingRequest> findTop5ByOrderByCreatedAtDesc();

    @Override
    @EntityGraph(attributePaths = {"requiredSkills"})
    List<StaffingRequest> findAll();

    @Override
    @EntityGraph(attributePaths = {"requiredSkills"})
    Optional<StaffingRequest> findById(Long id);
}
