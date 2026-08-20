package com.dpc.smart_staffing_backend.repository;

import com.dpc.smart_staffing_backend.entity.HRMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HRMemberRepository extends JpaRepository<HRMember, Long> {

    Optional<HRMember> findByEmail(String email);
}
