package com.acme.nubi.platform.visualroutine.interfaces.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** La rutina se prueba contra un perfil real creado con la API de Perfil. */
@SpringBootTest
class RoutineApiTests {

    private static final AtomicLong ACCOUNTS = new AtomicLong(70_000L);

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private long profileId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        var created = mockMvc.perform(post("/api/v1/profiles")
                        .header("X-Account-Id", ACCOUNTS.incrementAndGet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Tomás","lastName":"Vega","age":6}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        profileId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    void startsWithTheSuggestedRoutineAndMarksStepsAsDone() throws Exception {
        var routine = mockMvc.perform(get(base()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].title").value("Despertar"))
                .andExpect(jsonPath("$[0].completedToday").value(false))
                .andReturn();
        Integer firstId = JsonPath.read(routine.getResponse().getContentAsString(), "$[0].id");

        mockMvc.perform(post(base() + "/" + firstId + "/completion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedToday").value(true));

        mockMvc.perform(delete(base() + "/" + firstId + "/completion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedToday").value(false));
    }

    @Test
    void addsEditsAndRemovesAStep() throws Exception {
        var created = mockMvc.perform(post(base())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Terapia","icon":"🧸","scheduledTime":"17:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Terapia"))
                .andExpect(jsonPath("$.scheduledTime").value("17:00:00"))
                .andReturn();
        Integer stepId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(put(base() + "/" + stepId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Terapia de lenguaje","icon":"🗣️"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Terapia de lenguaje"));

        mockMvc.perform(post(base())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("routine.step.invalid"));

        mockMvc.perform(delete(base() + "/" + stepId)).andExpect(status().isNoContent());
        mockMvc.perform(get(base())).andExpect(jsonPath("$", hasSize(10)));
    }

    @Test
    void anUnknownProfileHasNoRoutine() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/987654/routine-steps"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("routine.profile.notFound"));
    }

    private String base() {
        return "/api/v1/profiles/" + profileId + "/routine-steps";
    }
}
