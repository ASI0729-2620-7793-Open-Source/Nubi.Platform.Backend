package com.acme.nubi.platform.selfregulation.interfaces.rest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SelfRegulationApiTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void listsTheSeededCatalogAndFiltersByType() throws Exception {
        mockMvc.perform(get("/api/v1/calming-resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].code").value("FLOATING_BUBBLES"));

        mockMvc.perform(get("/api/v1/calming-resources").param("type", "AUDITORY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/api/v1/calming-resources").param("type", "SMELL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void runsACalmSessionFromStartToFinish() throws Exception {
        var created = mockMvc.perform(post("/api/v1/calm-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 10, \"resourceId\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.intensity").value(6))
                .andExpect(jsonPath("$.timer").doesNotExist())
                .andReturn();
        Number sessionId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        var sessionUrl = "/api/v1/calm-sessions/" + sessionId.longValue();

        mockMvc.perform(put(sessionUrl + "/intensity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"intensity\": 25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intensity").value(10));

        mockMvc.perform(put(sessionUrl + "/low-stimulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStimulationActive").value(true));

        mockMvc.perform(post(sessionUrl + "/timer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"durationMinutes\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timer.remainingSeconds").value(60))
                .andExpect(jsonPath("$.timer.running").value(true));

        mockMvc.perform(post(sessionUrl + "/timer/ticks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"elapsedSeconds\": 75}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timer.remainingSeconds").value(0))
                .andExpect(jsonPath("$.shouldSuggestHelp").value(true));

        mockMvc.perform(post(sessionUrl + "/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.endedAt").exists())
                .andExpect(jsonPath("$.shouldSuggestHelp").value(false));

        mockMvc.perform(put(sessionUrl + "/intensity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"intensity\": 3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SelfRegulation.InvalidCalmSessionOperation"));

        mockMvc.perform(get("/api/v1/profiles/10/calm-sessions/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.longValue()));
    }

    @Test
    void rejectsInvalidCalmSessionRequests() throws Exception {
        mockMvc.perform(post("/api/v1/calm-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceId\": 1}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/calm-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 11, \"resourceId\": 999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SelfRegulation.CalmingResourceNotFound"));

        mockMvc.perform(get("/api/v1/calm-sessions/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void marksAndRemovesFavoriteResources() throws Exception {
        var favoritesUrl = "/api/v1/profiles/20/favorite-resources";

        mockMvc.perform(post(favoritesUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceId\": 2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").value(20))
                .andExpect(jsonPath("$.resourceId").value(2));

        mockMvc.perform(post(favoritesUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceId\": 2}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get(favoritesUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete(favoritesUrl + "/2"))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete(favoritesUrl + "/2"))
                .andExpect(status().isNotFound());
    }
}
