package com.acme.nubi.platform.assistivecomunication.interfaces;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Recorre la API del tablero CAA completa sobre una base H2 en memoria. */
@SpringBootTest
@AutoConfigureMockMvc
class CommunicationBoardApiTest {
    private static final String BASE = "/api/v1/profiles/%d/communication-board";

    @Autowired
    private MockMvc mockMvc;

    private String base(long profileId) {
        return BASE.formatted(profileId);
    }

    private long idFrom(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        var matcher = java.util.regex.Pattern.compile("\"id\":(\\d+)").matcher(body);
        matcher.find();
        return Long.parseLong(matcher.group(1));
    }

    @Test
    void profileWithoutBoardGetsTheDefaultPictograms() throws Exception {
        mockMvc.perform(get(base(101)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId", is(101)))
                .andExpect(jsonPath("$.categories", hasSize(3)))
                .andExpect(jsonPath("$.pictograms", hasSize(14)));

        // Pedirlo de nuevo no duplica el tablero
        mockMvc.perform(get(base(101)))
                .andExpect(jsonPath("$.pictograms", hasSize(14)));
    }

    @Test
    void quickAccessListsTheFavoritePictograms() throws Exception {
        mockMvc.perform(get(base(102) + "/quick-access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void createPictogramValidatesBodyAndCategory() throws Exception {
        String boardBody = mockMvc.perform(get(base(103))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // Los ids dependen del orden de los tests: se toma una categoría real del tablero
        var firstCategory = java.util.regex.Pattern.compile("\"categories\":\\[\\{\"id\":(\\d+)").matcher(boardBody);
        org.junit.jupiter.api.Assertions.assertTrue(firstCategory.find());
        String categoryId = firstCategory.group(1);

        // Sin etiqueta: 400
        mockMvc.perform(post(base(103) + "/pictograms").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrl\":\"perro.png\",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isBadRequest());

        // Imagen con formato no soportado: 400
        mockMvc.perform(post(base(103) + "/pictograms").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Perro\",\"imageUrl\":\"perro.pdf\",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isBadRequest());

        // Categoría inexistente: 404
        mockMvc.perform(post(base(103) + "/pictograms").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Perro\",\"imageUrl\":\"perro.png\",\"categoryId\":999999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void caregiverCanCustomizeTheBoard() throws Exception {
        // Se crea una categoría propia y un pictograma dentro de ella
        MvcResult category = mockMvc.perform(post(base(104) + "/categories")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Escuela\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type", is("CUSTOM")))
                .andReturn();
        long categoryId = idFrom(category);

        MvcResult pictogram = mockMvc.perform(post(base(104) + "/pictograms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Mochila\",\"imageUrl\":\"mochila.png\",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.custom", is(true)))
                .andExpect(jsonPath("$.phrase", is("Mochila")))
                .andExpect(jsonPath("$.categoryId", is((int) categoryId)))
                .andReturn();
        long pictogramId = idFrom(pictogram);

        // Nombre de categoría repetido: 409
        mockMvc.perform(post(base(104) + "/categories")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"escuela\"}"))
                .andExpect(status().isConflict());

        // Favorito y mover a otra categoría
        mockMvc.perform(put(base(104) + "/pictograms/" + pictogramId + "/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite", is(true)));
        mockMvc.perform(delete(base(104) + "/pictograms/" + pictogramId + "/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite", is(false)));
        mockMvc.perform(put(base(104) + "/pictograms/" + pictogramId + "/category")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"categoryId\":999999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void favoritesStopAtTheBoardLimit() throws Exception {
        // El tablero por defecto ya trae 6 favoritos de un máximo de 8
        MvcResult board = mockMvc.perform(get(base(105))).andExpect(status().isOk()).andReturn();
        String body = board.getResponse().getContentAsString();
        var matcher = java.util.regex.Pattern
                .compile("\\{\"id\":(\\d+),[^{}]*\"favorite\":false").matcher(body);

        int accepted = 0;
        int rejected = 0;
        while (matcher.find()) {
            int code = mockMvc.perform(put(base(105) + "/pictograms/" + matcher.group(1) + "/favorite"))
                    .andReturn().getResponse().getStatus();
            if (code == 200) {
                accepted++;
            } else if (code == 409) {
                rejected++;
            }
        }
        org.junit.jupiter.api.Assertions.assertEquals(2, accepted);
        org.junit.jupiter.api.Assertions.assertTrue(rejected > 0);
    }

    @Test
    void selectionFlowEndsAsNotUnderstood() throws Exception {
        MvcResult board = mockMvc.perform(get(base(106))).andReturn();
        String boardBody = board.getResponse().getContentAsString();
        // El id del primer pictograma va justo después de "pictograms":[
        var firstPictogram = java.util.regex.Pattern.compile("\"pictograms\":\\[\\{\"id\":(\\d+)").matcher(boardBody);
        org.junit.jupiter.api.Assertions.assertTrue(firstPictogram.find());
        long pictogramId = Long.parseLong(firstPictogram.group(1));

        MvcResult selection = mockMvc.perform(post(base(106) + "/pictograms/" + pictogramId + "/selections"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDING_DELIVERY")))
                .andReturn();
        long selectionId = idFrom(selection);

        mockMvc.perform(patch(base(106) + "/selections/" + selectionId + "/delivered"))
                .andExpect(jsonPath("$.status", is("DELIVERED")));
        mockMvc.perform(patch(base(106) + "/selections/" + selectionId + "/not-understood"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("NOT_UNDERSTOOD")));
        // Una selección resuelta no cambia de estado
        mockMvc.perform(patch(base(106) + "/selections/" + selectionId + "/understood"))
                .andExpect(status().isConflict());

        mockMvc.perform(get(base(106) + "/selections"))
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get(base(106) + "/selections?status=PENDING_DELIVERY"))
                .andExpect(jsonPath("$", hasSize(0)));

        // Una selección de otro perfil no es accesible
        mockMvc.perform(patch(base(107) + "/selections/" + selectionId + "/understood"))
                .andExpect(status().isNotFound());
    }

    @Test
    void moodCheckInsAreRegisteredNewestFirst() throws Exception {
        mockMvc.perform(post(base(108) + "/mood-check-ins").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"CALM\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level", is("CALM")));
        mockMvc.perform(post(base(108) + "/mood-check-ins").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"ALTERED\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post(base(108) + "/mood-check-ins").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"FURIOUS\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(base(108) + "/mood-check-ins"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].level", is("ALTERED")));
    }
}
