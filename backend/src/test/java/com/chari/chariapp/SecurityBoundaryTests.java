package com.chari.chariapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityBoundaryTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void passportDataRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/passport/1234567890"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nationalIdentityDataRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/national-ids/1234567890"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void enrollmentRequestIsPublicButLegacyRegistrationIsNot() throws Exception {
        mockMvc.perform(post("/auth/enrollment/otp")
                        .contentType("application/json")
                        .content("{\"nationalId\":\"ABC-123456\",\"phoneNumber\":\"+23599123456\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("{\"nationalId\":\"ABC-123456\",\"email\":\"test@example.com\",\"password\":\"a-secure-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void citizenPhoneOperationsRequireAnAuthenticatedEmployeeOrAdministrator() throws Exception {
        mockMvc.perform(post("/api/v1/operations/citizens/00000000-0000-0000-0000-000000000001/phone-verifications")
                        .contentType("application/json")
                        .content("{\"phone\":\"+23599123456\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void staffAccountAdministrationRequiresAnAuthenticatedAdministrator() throws Exception {
        mockMvc.perform(post("/api/v1/admin/staff-accounts")
                        .contentType("application/json")
                        .content("{\"email\":\"employee@example.com\",\"password\":\"a-secure-password\"}"))
                .andExpect(status().isUnauthorized());
    }
}
