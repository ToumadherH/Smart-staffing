package com.dpc.smart_staffing_backend.repository;

import com.dpc.smart_staffing_backend.entity.Interview;
import com.dpc.smart_staffing_backend.entity.InterviewStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Override
    @EntityGraph(attributePaths = {"consultant", "staffingRequest"})
    List<Interview> findAll();

    @EntityGraph(attributePaths = {"consultant", "staffingRequest"})
    List<Interview> findByStatusOrderByDateAscTimeAsc(InterviewStatus status);

    long countByStatus(InterviewStatus status);

    long countByStatusAndDateGreaterThanEqual(InterviewStatus status, LocalDate date);
}
