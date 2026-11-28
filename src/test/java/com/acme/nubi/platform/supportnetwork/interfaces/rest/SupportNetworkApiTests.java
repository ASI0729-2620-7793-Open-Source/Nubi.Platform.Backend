package com.acme.nubi.platform.supportnetwork.interfaces.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SupportNetworkApiTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // Regresión: el RecommendationRepositoryImpl de crisismanagement se confundía con la
    // implementación del RecommendationRepository de supportnetwork y el POST devolvía 400.
    @Test
    void createsAndListsARecommendation() throws Exception {
        mockMvc.perform(post("/api/v1/support-network/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 77, \"episodeRecordId\": 1, \"title\": \"Respirar\","
                                + " \"description\": \"d\", \"strategy\": \"s\", \"actionGuide\": \"g\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // TS-05: sin identificar la cuenta, la consulta responde 401.
        mockMvc.perform(get("/api/v1/support-network/recommendations").param("profileId", "77"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/support-network/recommendations").param("profileId", "77")
                        .header("X-Account-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Respirar"));
    }

    @Test
    void rejectsARecommendationWithoutEpisodeRecord() throws Exception {
        mockMvc.perform(post("/api/v1/support-network/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 78, \"title\": \"t\", \"description\": \"d\","
                                + " \"strategy\": \"s\", \"actionGuide\": \"g\"}"))
                .andExpect(status().isBadRequest());
    }
}
