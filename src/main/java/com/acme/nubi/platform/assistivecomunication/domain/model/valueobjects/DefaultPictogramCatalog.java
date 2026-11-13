package com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects;

import java.util.List;

import static com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.CategoryType.ACTIVITIES;
import static com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.CategoryType.BASIC_NEEDS;
import static com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.CategoryType.EMOTIONS;

/**
 * Pictogramas básicos con los que se inicia el tablero de un perfil sin configuración previa
 * (US-19, escenario 2). Es contenido que define Nubi.
 */
public final class DefaultPictogramCatalog {

    public static final List<PictogramTemplate> BASIC_PICTOGRAMS = List.of(
            new PictogramTemplate("Agua", "Tengo sed", "pictograms/water.png", BASIC_NEEDS, true),
            new PictogramTemplate("Comida", "Tengo hambre", "pictograms/food.png", BASIC_NEEDS, true),
            new PictogramTemplate("Baño", "Necesito ir al baño", "pictograms/bathroom.png", BASIC_NEEDS, false),
            new PictogramTemplate("Descanso", "Quiero descansar", "pictograms/rest.png", BASIC_NEEDS, false),
            new PictogramTemplate("Dolor", "Me duele algo", "pictograms/pain.png", BASIC_NEEDS, true),
            new PictogramTemplate("Ayuda", "Necesito ayuda", "pictograms/help.png", BASIC_NEEDS, true),

            new PictogramTemplate("Feliz", "Estoy feliz", "pictograms/happy.png", EMOTIONS, true),
            new PictogramTemplate("Triste", "Estoy triste", "pictograms/sad.png", EMOTIONS, false),
            new PictogramTemplate("Enojado", "Estoy enojado", "pictograms/angry.png", EMOTIONS, false),
            new PictogramTemplate("Asustado", "Tengo miedo", "pictograms/scared.png", EMOTIONS, false),

            new PictogramTemplate("Jugar", "Quiero jugar", "pictograms/play.png", ACTIVITIES, true),
            new PictogramTemplate("Música", "Quiero escuchar música", "pictograms/music.png", ACTIVITIES, false),
            new PictogramTemplate("Leer", "Quiero leer", "pictograms/read.png", ACTIVITIES, false),
            new PictogramTemplate("Salir", "Quiero salir", "pictograms/go-out.png", ACTIVITIES, false)
    );

    private DefaultPictogramCatalog() {
    }
}
