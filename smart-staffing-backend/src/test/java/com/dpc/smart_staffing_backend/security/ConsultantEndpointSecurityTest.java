package com.dpc.smart_staffing_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Unlike ConsultantControllerTest (which disables the security filter chain to test
// routing/validation in isolation), this boots the full application with the real
// Spring Security filter chain active, proving endpoints are genuinely protected.
// Read-only requests only, so there is no data to clean up afterwards.
@SpringBootTest
@AutoConfigureMockMvc
class ConsultantEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${hr.seed.email}")
    private String seedEmail;

    @Value("${hr.seed.password}")
    private String seedPassword;

    @Test
    void consultantsEndpoint_withoutCredentials_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/consultants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consultantsEndpoint_withSeededHrMemberCredentials_returnsOk() throws Exception {
        mockMvc.perform(get("/api/consultants").with(httpBasic(seedEmail, seedPassword)))
                .andExpect(status().isOk());
    }

    @Test
    void consultantsEndpoint_withWrongPassword_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/consultants").with(httpBasic(seedEmail, "wrong-password")))
                .andExpect(status().isUnauthorized());
    }
}
