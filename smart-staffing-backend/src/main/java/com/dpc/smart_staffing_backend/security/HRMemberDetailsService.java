package com.dpc.smart_staffing_backend.security;

import com.dpc.smart_staffing_backend.entity.HRMember;
import com.dpc.smart_staffing_backend.repository.HRMemberRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Spring Security calls this to look up whoever is trying to authenticate.
// Spring auto-detects this bean (no wiring needed) because it's the only UserDetailsService
// in the context. There is only one role in this application, so it's assigned unconditionally.
@Service
public class HRMemberDetailsService implements UserDetailsService {

    private final HRMemberRepository hrMemberRepository;

    public HRMemberDetailsService(HRMemberRepository hrMemberRepository) {
        this.hrMemberRepository = hrMemberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        HRMember hrMember = hrMemberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No HR member found with email " + email));

        return User.withUsername(hrMember.getEmail())
                .password(hrMember.getPassword())
                .roles("HR_MEMBER")
                .build();
    }
}
