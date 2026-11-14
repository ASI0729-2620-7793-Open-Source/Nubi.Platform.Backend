package com.acme.nubi.platform.crisismanagement.interfaces.rest;

import com.acme.nubi.platform.crisismanagement.domain.model.events.EpisodeFinishedEvent;
import com.acme.nubi.platform.crisismanagement.domain.model.events.EpisodeRegisteredEvent;
import com.acme.nubi.platform.crisismanagement.domain.model.events.SosModeActivatedEvent;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.repositories.RecommendationPersistenceRepository;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.repositories.SosSessionPersistenceRepository;
import com.acme.nubi.platform.profile.interfaces.acl.ProfilesContextFacade;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cada prueba usa sus propios perfiles porque la base de datos en memoria se comparte entre pruebas.
 */
@SpringBootTest
@RecordApplicationEvents
class CrisisManagementApiTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private SosSessionPersistenceRepository sosSessionPersistenceRepository;

    @Autowired
    private RecommendationPersistenceRepository recommendationPersistenceRepository;

    /** El perfil vive en otro Bounded Context: aquí se simula su fachada (ACL). */
    @MockitoBean
    private ProfilesContextFacade profilesContextFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(profilesContextFacade.existsActiveProfile(anyLong())).thenReturn(true);
    }

    // ---------------------------------------------------------------- guía de acción

    @Test
    void servesTheSeededActiveGuideWithItsSixSteps() throws Exception {
        mockMvc.perform(get("/api/v1/action-guides/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.totalSteps").value(6))
                .andExpect(jsonPath("$.steps", hasSize(6)))
                .andExpect(jsonPath("$.steps[0].stepOrder").value(1))
                .andExpect(jsonPath("$.steps[0].code").value("SENSORY_ISOLATION"))
                .andExpect(jsonPath("$.steps[0].mandatory").value(true))
                .andExpect(jsonPath("$.steps[0].relatedTrigger").value("AUDITORY"))
                .andExpect(jsonPath("$.steps[0].alternativeTechniques", hasSize(2)))
                .andExpect(jsonPath("$.steps[0].alternativeTechniques[0].code").value("NOISE_CANCELLING_HEADPHONES"))
                .andExpect(jsonPath("$.steps[1].mandatory").value(false))
                .andExpect(jsonPath("$.steps[3].code").value("BITE_TOOL"))
                .andExpect(jsonPath("$.steps[3].alternativeTechniques[0].code").value("CHEWABLE_SNACK"))
                .andExpect(jsonPath("$.steps[5].code").value("FINAL_CHECK"));
    }

    @Test
    void returnsNotFoundForAnUnknownGuide() throws Exception {
        mockMvc.perform(get("/api/v1/action-guides/999999")).andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- sesión SOS

    @Test
    void runsASosSessionFromActivationToAnEarlyFinish() throws Exception {
        var created = mockMvc.perform(post("/api/v1/sos-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 101, \"caregiverId\": 5, \"trigger\": \"TACTILE\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.caregiverAccountId").value(5))
                .andExpect(jsonPath("$.initialIntensity").value("ALTERED"))
                .andExpect(jsonPath("$.trigger").value("TACTILE"))
                .andExpect(jsonPath("$.totalSteps").value(6))
                // El paso táctil (BITE_TOOL, orden 4) pasa al principio de la guía.
                .andExpect(jsonPath("$.currentStepOrder").value(4))
                .andExpect(jsonPath("$.completedSteps", hasSize(0)))
                .andExpect(jsonPath("$.durationMinutes").value(0))
                .andReturn();
        Number sessionId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        var sessionUrl = "/api/v1/sos-sessions/" + sessionId.longValue();
        assertThat(applicationEvents.stream(SosModeActivatedEvent.class))
                .anyMatch(event -> event.sessionId().equals(sessionId.longValue()) && event.profileId().equals(101L));

        // Solo una sesión en curso por perfil.
        mockMvc.perform(post("/api/v1/sos-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 101, \"caregiverAccountId\": 5}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CrisisManagement.ActiveSosSessionAlreadyExists"));
        mockMvc.perform(get("/api/v1/profiles/101/sos-sessions/in-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.longValue()));

        // La guía de la sesión empieza por el paso del detonante.
        var guide = mockMvc.perform(get(sessionUrl + "/guide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps", hasSize(6)))
                .andExpect(jsonPath("$.steps[0].code").value("BITE_TOOL"))
                .andExpect(jsonPath("$.steps[1].code").value("SENSORY_ISOLATION"))
                .andReturn();
        List<Number> stepIds = JsonPath.read(guide.getResponse().getContentAsString(), "$.steps[*].id");
        var biteToolId = stepIds.get(0).longValue();
        var isolationId = stepIds.get(1).longValue();
        var validationId = stepIds.get(2).longValue();

        mockMvc.perform(patch(sessionUrl + "/steps/" + biteToolId).contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedSteps", hasSize(1)))
                .andExpect(jsonPath("$.completedSteps[0].skipped").value(false))
                // Sigue el primer paso pendiente de la guía personalizada: el aislamiento sensorial.
                .andExpect(jsonPath("$.currentStepOrder").value(1));

        // Completar dos veces el mismo paso no cambia nada.
        mockMvc.perform(patch(sessionUrl + "/steps/" + biteToolId).contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedSteps", hasSize(1)));

        // Un paso obligatorio no se omite sin confirmación.
        mockMvc.perform(patch(sessionUrl + "/steps/" + isolationId).contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"SKIPPED\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch(sessionUrl + "/steps/" + isolationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"SKIPPED\", \"confirmed\": false}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch(sessionUrl + "/steps/" + isolationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"SKIPPED\", \"confirmed\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedSteps", hasSize(2)))
                .andExpect(jsonPath("$.completedSteps[1].skipped").value(true))
                .andExpect(jsonPath("$.currentStepOrder").value(2));

        // Un paso opcional sí se puede omitir sin confirmar.
        mockMvc.perform(patch(sessionUrl + "/steps/" + validationId).contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"SKIPPED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedSteps", hasSize(3)));

        // Técnica alternativa: el aislamiento tiene dos, la validación emocional ninguna.
        mockMvc.perform(post(sessionUrl + "/steps/" + isolationId + "/alternatives"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("NOISE_CANCELLING_HEADPHONES"))
                .andExpect(jsonPath("$.stepId").value(isolationId));
        mockMvc.perform(post(sessionUrl + "/steps/" + validationId + "/alternatives"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CrisisManagement.AlternativeTechniqueNotFound"));
        mockMvc.perform(patch(sessionUrl + "/steps/999999").contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CrisisManagement.StepNotFound"));

        // Faltan pasos: la sesión termina antes de tiempo.
        mockMvc.perform(patch(sessionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"FINISHED\", \"observedState\": \"TIRED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED_EARLY"))
                .andExpect(jsonPath("$.observedState").value("TIRED"))
                .andExpect(jsonPath("$.finalIntensity").value("RESTLESS"))
                .andExpect(jsonPath("$.finishedAt").exists())
                .andExpect(jsonPath("$.durationMinutes").value(1));
        assertThat(applicationEvents.stream(EpisodeFinishedEvent.class))
                .anyMatch(event -> event.sessionId().equals(sessionId.longValue())
                        && event.completedSteps() == 3
                        && event.totalSteps() == 6
                        && event.trigger() == SensoryTrigger.TACTILE);

        // Una sesión cerrada no admite cambios y deja de ser la sesión en curso.
        mockMvc.perform(patch(sessionUrl).contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"FINISHED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CrisisManagement.InvalidSosSessionOperation"));
        mockMvc.perform(patch(sessionUrl + "/steps/" + biteToolId).contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/v1/profiles/101/sos-sessions/in-progress"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/profiles/101/sos-sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void finishesNormallyWhenEveryStepWasCompletedAndThenAllowsANewSession() throws Exception {
        var created = mockMvc.perform(post("/api/v1/sos-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 102, \"caregiverAccountId\": 6, \"initialIntensity\": \"CRISIS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.initialIntensity").value("CRISIS"))
                .andExpect(jsonPath("$.trigger").value("NONE"))
                .andExpect(jsonPath("$.currentStepOrder").value(1))
                .andReturn();
        Number sessionId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        var sessionUrl = "/api/v1/sos-sessions/" + sessionId.longValue();

        var guide = mockMvc.perform(get(sessionUrl + "/guide")).andReturn();
        List<Number> stepIds = JsonPath.read(guide.getResponse().getContentAsString(), "$.steps[*].id");
        for (var stepId : stepIds) {
            mockMvc.perform(patch(sessionUrl + "/steps/" + stepId.longValue()).contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"COMPLETED\"}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get(sessionUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedSteps", hasSize(6)));
        mockMvc.perform(patch(sessionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"FINISHED\", \"observedState\": \"CALM\", \"finalIntensity\": \"CALM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.finalIntensity").value("CALM"));

        mockMvc.perform(post("/api/v1/sos-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 102, \"caregiverAccountId\": 6}"))
                .andExpect(status().isCreated());
    }

    @Test
    void keepsTheAuditColumnsWhenTheSessionIsUpdated() throws Exception {
        var created = mockMvc.perform(post("/api/v1/sos-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 103, \"caregiverAccountId\": 7}"))
                .andExpect(status().isCreated())
                .andReturn();
        Number sessionId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(patch("/api/v1/sos-sessions/" + sessionId.longValue()).contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"FINISHED\"}"))
                .andExpect(status().isOk());

        var saved = sosSessionPersistenceRepository.findById(sessionId.longValue()).orElseThrow();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull().isAfterOrEqualTo(saved.getCreatedAt());
    }

    @Test
    void rejectsInvalidSosSessionRequests() throws Exception {
        mockMvc.perform(post("/api/v1/sos-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caregiverAccountId\": 5}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/sos-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 104}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/sos-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 104, \"caregiverAccountId\": 5, \"trigger\": \"SMELL\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/sos-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileId\": 104, \"caregiverAccountId\": 5, \"initialIntensity\": \"FURIOUS\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/profiles/104/sos-sessions")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void returnsNotFoundForAnUnknownSession() throws Exception {
        mockMvc.perform(get("/api/v1/sos-sessions/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/sos-sessions/999999/guide"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CrisisManagement.SosSessionNotFound"));
        mockMvc.perform(patch("/api/v1/sos-sessions/999999/steps/1").contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CrisisManagement.SosSessionNotFound"));
        mockMvc.perform(patch("/api/v1/sos-sessions/999999").contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"FINISHED\"}")).andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- recomendaciones

    @Test
    void generatesOneRecommendationPerTopicWithoutDuplicatingThem() throws Exception {
        var first = mockMvc.perform(post("/api/v1/profiles/301/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].topic").value("SENSORY"))
                .andExpect(jsonPath("$[1].topic").value("COMMUNICATIONAL"))
                .andExpect(jsonPath("$[2].topic").value("BEHAVIORAL"))
                .andExpect(jsonPath("$[0].version").value(1))
                .andExpect(jsonPath("$[0].outdated").value(false))
                .andReturn();
        List<Number> firstIds = JsonPath.read(first.getResponse().getContentAsString(), "$[*].id");

        var second = mockMvc.perform(post("/api/v1/profiles/301/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andReturn();
        List<Number> secondIds = JsonPath.read(second.getResponse().getContentAsString(), "$[*].id");
        assertThat(secondIds).isEqualTo(firstIds);

        mockMvc.perform(get("/api/v1/profiles/301/recommendations").param("topic", "COMMUNICATIONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].topic").value("COMMUNICATIONAL"));
        mockMvc.perform(get("/api/v1/profiles/301/recommendations").param("topic", "MUSICAL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ratesARecommendationOncePerCaregiver() throws Exception {
        var generated = mockMvc.perform(post("/api/v1/profiles/302/recommendations")).andReturn();
        Number recommendationId = JsonPath.read(generated.getResponse().getContentAsString(), "$[0].id");
        var url = "/api/v1/recommendations/" + recommendationId.longValue() + "/preferences";

        mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\": 9, \"usefulnessRating\": 5, \"favorite\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences", hasSize(1)))
                .andExpect(jsonPath("$.preferences[0].usefulnessRating").value(5))
                .andExpect(jsonPath("$.preferences[0].favorite").value(true));

        // Volver a calificar actualiza la preferencia y conserva lo que no se envía.
        mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\": 9, \"usefulnessRating\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences", hasSize(1)))
                .andExpect(jsonPath("$.preferences[0].usefulnessRating").value(3))
                .andExpect(jsonPath("$.preferences[0].favorite").value(true));

        mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\": 10, \"favorite\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences", hasSize(2)));

        mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\": 9, \"usefulnessRating\": 9}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\": 9}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/v1/recommendations/999999/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\": 9, \"usefulnessRating\": 4}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CrisisManagement.RecommendationNotFound"));

        var saved = recommendationPersistenceRepository.findById(recommendationId.longValue()).orElseThrow();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull().isAfterOrEqualTo(saved.getCreatedAt());
    }

    @Test
    void anEpisodeWithNewInformationReplacesTheRecommendationsWithANewVersion() throws Exception {
        var generated = mockMvc.perform(post("/api/v1/profiles/303/recommendations")).andReturn();
        Number sensoryId = JsonPath.read(generated.getResponse().getContentAsString(), "$[0].id");

        // Un episodio sin información nueva no cambia nada.
        eventPublisher.publishEvent(new EpisodeRegisteredEvent(1L, 303L, SensoryTrigger.AUDITORY, false));
        mockMvc.perform(get("/api/v1/profiles/303/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sensoryId.longValue()))
                .andExpect(jsonPath("$[0].version").value(1));

        eventPublisher.publishEvent(new EpisodeRegisteredEvent(2L, 303L, SensoryTrigger.AUDITORY, true));

        // Solo cambian las recomendaciones cuyo contenido depende del detonante: la sensorial.
        mockMvc.perform(get("/api/v1/profiles/303/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].topic").value("SENSORY"))
                .andExpect(jsonPath("$[0].version").value(2))
                .andExpect(jsonPath("$[0].content").value(org.hamcrest.Matchers.containsString("sonidos")))
                .andExpect(jsonPath("$[1].version").value(1))
                .andExpect(jsonPath("$[2].version").value(1));

        // La versión anterior se conserva, marcada como desactualizada, y ya no se puede calificar.
        var old = recommendationPersistenceRepository.findById(sensoryId.longValue()).orElseThrow();
        assertThat(old.isOutdated()).isTrue();
        mockMvc.perform(put("/api/v1/recommendations/" + sensoryId.longValue() + "/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\": 9, \"usefulnessRating\": 4}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CrisisManagement.InvalidRecommendationOperation"));
    }

    @Test
    void anEpisodeGeneratesTheRecommendationsOfAProfileThatHasNone() throws Exception {
        eventPublisher.publishEvent(new EpisodeRegisteredEvent(3L, 304L, SensoryTrigger.VISUAL, true));

        mockMvc.perform(get("/api/v1/profiles/304/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].content").value(org.hamcrest.Matchers.containsString("visual")));
    }
}
