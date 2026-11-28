package com.acme.nubi.platform.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AccessTokenApiTests {

    private static final String RECOMMENDATIONS = "/api/v1/support-network/recommendations?profileId=1";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AccessTokenService tokens;

    @Autowired
    private AccessTokenFilter filter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(filter).build();
    }

    @Test
    @DisplayName("TS-05 E2: recomendaciones sin token válido responden 401 problem+json")
    void rejectsMissingToken() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        mockMvc.perform(get(RECOMMENDATIONS).header("X-Account-Id", 1))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un token alterado responde 401 y uno válido 200")
    void verifiesSignature() throws Exception {
        String token = tokens.issue(1L);
        mockMvc.perform(get(RECOMMENDATIONS).header("Authorization", "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(RECOMMENDATIONS).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        assertThat(tokens.verify(token)).contains(1L);
    }

    @Test
    @DisplayName("El registro entrega un token y el catálogo sigue abierto")
    void signUpReturnsToken() throws Exception {
        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"token.test@nubi.app\",\"fullName\":\"Token Test\",\"password\":\"clave-de-prueba-1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
        mockMvc.perform(get("/api/v1/calming-resources")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Recursos inexistentes responden 404 problem+json")
    void notFoundHasProblemBody() throws Exception {
        String bearer = "Bearer " + tokens.issue(1L);
        mockMvc.perform(get("/api/v1/sos-sessions/999999").header("Authorization", bearer))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        mockMvc.perform(get("/api/v1/profiles/999999/recommendations").header("Authorization", bearer))
                .andExpect(status().isNotFound());
    }
}
