package com.chari.chariapp.account.infrastructure.web;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.PasswordHasher;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.EmailReference;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import com.chari.chariapp.citizen.domain.PhoneReference;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountStore accountStore;

    @Autowired
    private CitizenStore citizenStore;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private PersonalDataProtector dataProtector;

    @Test
    void logsInAndRotatesTheRefreshTokenThroughTheHttpApi() throws Exception {
        String email = "citizen-" + UUID.randomUUID() + "@example.com";
        String password = "a-secure-password";
        CitizenId citizenId = CitizenId.newId();
        String registryReference = "CID001";
        citizenStore.save(new Citizen(
                citizenId,
                new NationalIdReference(dataProtector.lookup(registryReference), dataProtector.encrypt(registryReference)),
                new PhoneReference(dataProtector.lookup("phone-" + registryReference), dataProtector.encrypt("phone-" + registryReference)),
                Instant.now(),
                Instant.now()
        ));
        accountStore.save(new Account(
                AccountId.newId(), citizenId,
                new EmailReference(dataProtector.lookup(email), dataProtector.encrypt(email)),
                passwordHasher.hash(password), AccountStatus.ACTIVE, Set.of(AccountRole.CITIZEN), Instant.now()
        ));

        String login = response(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nationalId\":\"cid001\",\"password\":\"%s\"}".formatted(password)))
                .andExpect(status().isOk())
                .andReturn());
        String initialRefreshToken = jsonField(login, "refreshToken");

        for (String invalidBody : new String[] {
                "{\"nationalId\":\"CID001\",\"password\":\"wrong-password\"}",
                "{\"nationalId\":\"CID999\",\"password\":\"a-secure-password\"}"
        }) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON).content(invalidBody))
                    .andExpect(status().isUnauthorized());
        }

        String refresh = response(mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(initialRefreshToken)))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(jsonField(refresh, "accessToken")).isNotBlank();
        assertThat(jsonField(refresh, "refreshToken")).isNotEqualTo(initialRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(initialRefreshToken)))
                .andExpect(status().isUnauthorized());
    }

    private static String response(org.springframework.test.web.servlet.MvcResult result) throws Exception {
        return result.getResponse().getContentAsString();
    }

    private static String jsonField(String json, String field) {
        Matcher matcher = Pattern.compile("\\\"" + field + "\\\":\\\"([^\\\"]+)\\\"").matcher(json);
        assertThat(matcher.find()).as("JSON field " + field).isTrue();
        return matcher.group(1);
    }
}
