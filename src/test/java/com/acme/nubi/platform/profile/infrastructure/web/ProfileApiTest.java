package com.acme.nubi.platform.profile.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Pruebas del contrato REST contra H2 en memoria: no tocan MySQL.
 *
 * <p>Cubren los códigos que exige el informe: 201 al crear, 400 con el detalle
 * del campo, 404 si no existe y 409 al superar un límite del plan.
 */
@SpringBootTest
class ProfileApiTest {

    private static final String ACCOUNT = "X-Account-Id";
    /** Cada prueba usa una cuenta distinta para no heredar datos entre tests. */
    private static final java.util.concurrent.atomic.AtomicLong ACCOUNTS =
            new java.util.concurrent.atomic.AtomicLong(10_000L);

    private long accountId;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        accountId = ACCOUNTS.incrementAndGet();
    }

    // --- US-01: crear y consultar ---------------------------------------

    @Test
    @DisplayName("POST /profiles devuelve 201 con el perfil recién creado")
    void createProfileReturns201() throws Exception {
        mockMvc.perform(post("/api/v1/profiles")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("Lucía", "Fernández", "Luchi", 8)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("Lucía"))
                .andExpect(jsonPath("$.displayName").value("Luchi"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.sensoryProfile.auditory").value("MEDIUM"))
                .andExpect(jsonPath("$.caregivers[0].role").value("PRIMARY"))
                .andExpect(jsonPath("$.caregivers[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /profiles sin nombre responde 400 con el campo inválido")
    void createWithoutFirstNameReturns400WithFieldDetail() throws Exception {
        mockMvc.perform(post("/api/v1/profiles")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"  ","lastName":"Fernández","age":8}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("request.validation"))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.field").value("firstName"));
    }

    @Test
    @DisplayName("POST /profiles sin cabecera X-Account-Id responde 400")
    void createWithoutAccountHeaderReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("Lucía", "Fernández", "Luchi", 8)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /profiles lista los perfiles de la cuenta (US-01)")
    void listProfilesReturns200() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(get("/api/v1/profiles").header(ACCOUNT, accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(profileId))
                .andExpect(jsonPath("$[0].firstName").value("Lucía"));
    }

    @Test
    @DisplayName("GET /profiles/{id} devuelve 404 cuando no existe")
    void getUnknownProfileReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/999999").header(ACCOUNT, accountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("profile.notFound"));
    }

    @Test
    @DisplayName("GET /profiles/{id} de una cuenta ajena también es 404 (no revela existencia)")
    void getProfileOfAnotherAccountReturns404() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(get("/api/v1/profiles/" + profileId).header(ACCOUNT, 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("profile.notAccessible"));
    }

    // --- US-02: editar ficha general ------------------------------------

    @Test
    @DisplayName("PUT /profiles/{id} actualiza la ficha general (US-02)")
    void updateGeneralDataReturns200() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(put("/api/v1/profiles/" + profileId)
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Lucía","lastName":"Fernández","nickname":"Luchi",
                                 "age":9,"gender":"FEMALE","communicativeNeed":"VOICE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.age").value(9))
                .andExpect(jsonPath("$.communicativeNeed").value("VOICE"));
    }

    @Test
    @DisplayName("PUT /profiles/{id} con edad 0 responde 400 con el campo age")
    void updateWithInvalidAgeReturns400() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(put("/api/v1/profiles/" + profileId)
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Lucía","lastName":"Fernández","age":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.age").exists());
    }

    // --- US-03: sensibilidades sensoriales ------------------------------

    @Test
    @DisplayName("PUT /profiles/{id}/sensory-profile reemplaza el bloque completo (US-03)")
    void updateSensoryProfileReplacesWholeBlock() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(put("/api/v1/profiles/" + profileId + "/sensory-profile")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"auditory":"VERY_HIGH","visual":"LOW","tactile":"HIGH",
                                 "lowStimulationEnabled":true,"prioritizeVisuals":true,
                                 "confirmAudio":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditory").value("VERY_HIGH"))
                .andExpect(jsonPath("$.visual").value("LOW"))
                .andExpect(jsonPath("$.lowStimulationEnabled").value(true));

        mockMvc.perform(get("/api/v1/profiles/" + profileId + "/sensory-profile")
                        .header(ACCOUNT, accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditory").value("VERY_HIGH"))
                .andExpect(jsonPath("$.tactile").value("HIGH"));
    }

    @Test
    @DisplayName("PUT /profiles/{id}/sensory-profile sin nivel es 400 con el campo")
    void updateSensoryProfileWithoutLevelReturns400() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(put("/api/v1/profiles/" + profileId + "/sensory-profile")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"auditory":"HIGH"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.visual").exists());
    }

    // --- US-04: diagnóstico declarado -----------------------------------

    @Test
    @DisplayName("PUT /profiles/{id}/diagnosis registra un diagnóstico declarado (US-04)")
    void updateDiagnosisReturns200() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(put("/api/v1/profiles/" + profileId + "/diagnosis")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"condition":"ASD","diagnosedBy":"Dra. Gómez",
                                 "diagnosisDate":"2024-05-01","professionalNotes":"A"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value("ASD"))
                .andExpect(jsonPath("$.diagnosedBy").value("Dra. Gómez"));
    }

    @Test
    @DisplayName("Nubi solo acompaña a niños con autismo: otra condición responde 400")
    void diagnosisOtherThanAutismReturns400() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(put("/api/v1/profiles/" + profileId + "/diagnosis")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"condition":"ADHD"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("condition.autism.only"))
                .andExpect(jsonPath("$.field").value("condition"));
    }

    @Test
    @DisplayName("Todo perfil nace con el diagnóstico de autismo (ASD) declarado")
    void newProfileStartsWithAutismDiagnosis() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(get("/api/v1/profiles/" + profileId + "/diagnosis")
                        .header(ACCOUNT, accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value("ASD"));
    }

    @Test
    @DisplayName("El diagnóstico se puede completar sin enviar la condición (siempre ASD)")
    void diagnosisWithoutConditionDefaultsToAutism() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(put("/api/v1/profiles/" + profileId + "/diagnosis")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"diagnosedBy":"Dr. Rojas","professionalNotes":"Nivel 1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value("ASD"))
                .andExpect(jsonPath("$.diagnosedBy").value("Dr. Rojas"));
    }

    // --- US-05: foto -----------------------------------------------------

    @Test
    @DisplayName("PUT /profiles/{id}/photo guarda la URL (US-05)")
    void updatePhotoReturns200() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(put("/api/v1/profiles/" + profileId + "/photo")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"photoUrl":"/uploads/lucia.png"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").value("/uploads/lucia.png"));
    }

    // --- US-06: cuidadores asociados -------------------------------------

    @Test
    @DisplayName("POST /profiles/{id}/caregivers devuelve 201 con la invitación PENDING")
    void inviteCaregiverReturns201Pending() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(post("/api/v1/profiles/" + profileId + "/caregivers")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"pedro@nubi.app","role":"THERAPIST"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.invitedEmail").value("pedro@nubi.app"))
                .andExpect(jsonPath("$.role").value("THERAPIST"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Invitar el mismo correo dos veces responde 409 (US-06)")
    void duplicateCaregiverEmailReturns409() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);
        inviteCaregiver(profileId, "pedro@nubi.app");

        mockMvc.perform(post("/api/v1/profiles/" + profileId + "/caregivers")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"Pedro@nubi.app","role":"CAREGIVER"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("caregiver.email.duplicated"));
    }

    @Test
    @DisplayName("El cuidador principal no se puede revocar: 409 (US-06 regla 1)")
    void revokingPrimaryCaregiverReturns409() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);
        long primaryId = caregiverIdByEmail(profileId, "ana@nubi.app");

        mockMvc.perform(delete("/api/v1/profiles/" + profileId + "/caregivers/" + primaryId)
                        .header(ACCOUNT, accountId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("caregiver.principal.notRevocable"));
    }

    @Test
    @DisplayName("Un cuidador invitado acepta, puede leer el perfil y luego se revoca (US-06)")
    void invitedCaregiverCanAcceptThenReadAndBeRevoked() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);
        long caregiverId = inviteCaregiver(profileId, "pedro@nubi.app");

        mockMvc.perform(post("/api/v1/profiles/" + profileId + "/caregivers/"
                        + caregiverId + "/accept")
                        .header(ACCOUNT, 555L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.accountId").value(555));

        // La cuenta 555 ya es cuidador activa y puede leer el perfil.
        mockMvc.perform(get("/api/v1/profiles/" + profileId).header(ACCOUNT, 555L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Lucía"));

        mockMvc.perform(delete("/api/v1/profiles/" + profileId + "/caregivers/" + caregiverId)
                        .header(ACCOUNT, accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));

        // Tras la revocación pierde el acceso.
        mockMvc.perform(get("/api/v1/profiles/" + profileId).header(ACCOUNT, 555L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("FREEMIUM permite 1 cuidador invitado; el segundo es 409 (US-06)")
    void freemiumCaregiverLimitReturns409() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);
        inviteCaregiver(profileId, "pedro@nubi.app");

        mockMvc.perform(post("/api/v1/profiles/" + profileId + "/caregivers")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"maria@nubi.app","role":"THERAPIST"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("subscription.caregiver.limit"))
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("2 cuidadores por perfil")));
    }

    // --- Límites de plan --------------------------------------------------

    @Test
    @DisplayName("Una cuenta acompaña a un solo niño: el segundo perfil responde 409")
    void secondProfileReturns409() throws Exception {
        createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(post("/api/v1/profiles")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("Leo", "Fernández", "Leo", 2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("profile.single.child"));
    }

    @Test
    @DisplayName("Ni subiendo a FAMILY_PREMIUM se puede tener un segundo niño (US-48)")
    void upgradingPlanKeepsSingleChild() throws Exception {
        createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(put("/api/v1/subscriptions/current/plan")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"plan":"FAMILY_PREMIUM"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("FAMILY_PREMIUM"))
                .andExpect(jsonPath("$.maxProfiles").value(1))
                .andExpect(jsonPath("$.maxCaregiversPerProfile").value(5));

        mockMvc.perform(post("/api/v1/profiles")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("Leo", "Fernández", "Leo", 2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Archivar al niño libera el cupo para registrar otro perfil")
    void archivingReleasesSingleChildSlot() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(delete("/api/v1/profiles/" + profileId).header(ACCOUNT, accountId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/profiles")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("Leo", "Fernández", "Leo", 4)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /subscriptions/current nace en FREEMIUM con sus límites (US-46)")
    void subscriptionStartsFreemium() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions/current").header(ACCOUNT, accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("FREEMIUM"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.maxProfiles").value(1))
                .andExpect(jsonPath("$.maxCaregiversPerProfile").value(2));
    }

    @Test
    @DisplayName("PUT /subscriptions/current/plan con un plan desconocido es 400")
    void unknownPlanReturns400() throws Exception {
        mockMvc.perform(put("/api/v1/subscriptions/current/plan")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"plan":"GOLD"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("request.malformed"))
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("GOLD")));
    }

    // --- Contactos de confianza ------------------------------------------

    @Test
    @DisplayName("Vincular, listar, reordenar y desvincular contactos de confianza")
    void trustedContactsLifecycle() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(post("/api/v1/profiles/" + profileId + "/trusted-contacts")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Mamá","phone":"+51 999 000 111",
                                 "email":"mama@nubi.app","relationship":"Madre",
                                 "priorityOrder":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Mamá"))
                .andExpect(jsonPath("$.priorityOrder").value(1));

        mockMvc.perform(post("/api/v1/profiles/" + profileId + "/trusted-contacts")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Papá","phone":"+51 999 000 222",
                                 "relationship":"Padre","priorityOrder":2}
                                """))
                .andExpect(status().isCreated());

        // Misma prioridad => conflicto.
        mockMvc.perform(post("/api/v1/profiles/" + profileId + "/trusted-contacts")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Abuela","phone":"+51 999 000 333",
                                 "priorityOrder":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("contact.priorityOrder.duplicated"));

        List<Map<String, Object>> contacts = listTrustedContacts(profileId);
        if (contacts.size() != 2) {
            throw new AssertionError("Se esperaban 2 contactos: " + contacts);
        }
        long firstId = ((Number) contacts.get(0).get("id")).longValue();
        long secondId = ((Number) contacts.get(1).get("id")).longValue();

        mockMvc.perform(put("/api/v1/profiles/" + profileId + "/trusted-contacts/order")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"order":[%d,%d]}
                                """.formatted(secondId, firstId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Papá"))
                .andExpect(jsonPath("$[0].priorityOrder").value(1))
                .andExpect(jsonPath("$[1].fullName").value("Mamá"))
                .andExpect(jsonPath("$[1].priorityOrder").value(2));

        mockMvc.perform(delete("/api/v1/profiles/" + profileId
                        + "/trusted-contacts/" + secondId)
                        .header(ACCOUNT, accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Mamá"))
                .andExpect(jsonPath("$[0].priorityOrder").value(1));
    }

    @Test
    @DisplayName("Vincular un contacto sin nombre responde 400 con el campo")
    void linkContactWithoutNameReturns400() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(post("/api/v1/profiles/" + profileId + "/trusted-contacts")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"+51 999 000 111","priorityOrder":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }

    // --- Archivado --------------------------------------------------------

    @Test
    @DisplayName("DELETE /profiles/{id} archiva el perfil sin borrarlo (US-01)")
    void archiveProfileDisablesIt() throws Exception {
        long profileId = createProfile("Lucía", "Fernández", "Luchi", 8);

        mockMvc.perform(delete("/api/v1/profiles/" + profileId).header(ACCOUNT, accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.firstName").value("Lucía"));

        // Un perfil archivado ya no aparece en el listado.
        mockMvc.perform(get("/api/v1/profiles").header(ACCOUNT, accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- Ayudantes --------------------------------------------------------

    private String createBody(String firstName, String lastName, String nickname, int age) {
        return """
                {"firstName":"%s","lastName":"%s","nickname":"%s","age":%d,
                 "gender":"FEMALE","communicativeNeed":"PICTOGRAMS",
                 "primaryCaregiverEmail":"ana@nubi.app"}
                """.formatted(firstName, lastName, nickname, age);
    }

    private long createProfile(String firstName, String lastName, String nickname, int age)
            throws Exception {
        String response = mockMvc.perform(post("/api/v1/profiles")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(firstName, lastName, nickname, age)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return number(json(response), "id");
    }

    private long inviteCaregiver(long profileId, String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/profiles/" + profileId + "/caregivers")
                        .header(ACCOUNT, accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","role":"CAREGIVER"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return number(json(response), "id");
    }

    private long caregiverIdByEmail(long profileId, String email) throws Exception {
        String response = mockMvc.perform(get("/api/v1/profiles/" + profileId + "/caregivers")
                        .header(ACCOUNT, accountId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        for (Map<String, Object> caregiver : listOf(response)) {
            if (email.equalsIgnoreCase(String.valueOf(caregiver.get("invitedEmail")))) {
                return number(caregiver, "id");
            }
        }
        throw new AssertionError("No se encontró el cuidador " + email + " en " + response);
    }

    private List<Map<String, Object>> listTrustedContacts(long profileId) throws Exception {
        String response = mockMvc.perform(
                        get("/api/v1/profiles/" + profileId + "/trusted-contacts")
                                .header(ACCOUNT, accountId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return listOf(response);
    }

    private Map<String, Object> json(String body) throws Exception {
        return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
        });
    }

    private List<Map<String, Object>> listOf(String body) throws Exception {
        return objectMapper.readValue(body, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    private static long number(Map<String, Object> source, String field) {
        Object value = source.get(field);
        if (!(value instanceof Number number)) {
            throw new AssertionError("El campo " + field + " no es numérico: " + source);
        }
        return number.longValue();
    }
}
