package com.acme.nubi.platform.supportnetwork.interfaces.rest;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Rutas REST sin verbos de Red de Apoyo: el cambio de estado va en PATCH sobre el recurso,
 * compartir crea un sub-recurso "shares" y el filtro por fechas va en query params.
 */
@SpringBootTest
class SupportNetworkRestConventionsTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void confirmsAndRejectsHelpRequestStatusChangesWithPatch() throws Exception {
        var created = mockMvc.perform(post("/api/v1/help-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 81, \"message\": \"Necesito apoyo\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(patch("/api/v1/help-requests/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"PENDING\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/help-requests/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Un pedido ya atendido no se puede cancelar.
        mockMvc.perform(patch("/api/v1/help-requests/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CANCELLED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void sharesAReportByCreatingAShare() throws Exception {
        var created = mockMvc.perform(post("/api/v1/episode-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 82, \"periodStart\": \"2026-11-01T00:00:00\","
                                + " \"periodEnd\": \"2026-11-07T23:59:00\", \"episodeCount\": 2,"
                                + " \"summary\": \"Semana tranquila\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/v1/episode-reports/" + id + "/shares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sharedWith\": \"Dra. Paredes\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shared").value(true))
                .andExpect(jsonPath("$.sharedWith").value("Dra. Paredes"));
    }

    @Test
    void marksARecommendationAsEffectiveWithPatch() throws Exception {
        var created = mockMvc.perform(post("/api/v1/support-network/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 83, \"episodeRecordId\": 1, \"title\": \"Audífonos\","
                                + " \"description\": \"d\", \"strategy\": \"s\", \"actionGuide\": \"g\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(patch("/api/v1/support-network/recommendations/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"effective\": false}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/support-network/recommendations/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"effective\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effective").value(true));
    }

    @Test
    void filtersEpisodeRecordsByDateWithQueryParams() throws Exception {
        mockMvc.perform(post("/api/v1/episode-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 84, \"sosSessionId\": 1, \"emotionalState\": \"CALM\","
                                + " \"startedAt\": \"2026-11-03T10:00:00\", \"endedAt\": \"2026-11-03T10:10:00\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/episode-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 84, \"sosSessionId\": 2, \"emotionalState\": \"ALTERED\","
                                + " \"startedAt\": \"2026-11-10T18:00:00\", \"endedAt\": \"2026-11-10T18:20:00\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/episode-records").param("profileId", "84"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/v1/episode-records")
                        .param("profileId", "84")
                        .param("startDate", "2026-11-01T00:00:00")
                        .param("endDate", "2026-11-05T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sosSessionId").value(1));
    }
}
