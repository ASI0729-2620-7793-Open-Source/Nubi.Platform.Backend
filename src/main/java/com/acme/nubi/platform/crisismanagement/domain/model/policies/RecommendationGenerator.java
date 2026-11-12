package com.acme.nubi.platform.crisismanagement.domain.model.policies;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.RecommendationTopic;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Política que arma las recomendaciones de un perfil según su detonante principal. Es una guía de
 * apoyo cotidiano: no reemplaza la atención de un profesional de la salud.
 */
public final class RecommendationGenerator {

    private RecommendationGenerator() {
    }

    /** Una recomendación por tópico, ajustada al detonante del perfil. */
    public static List<Recommendation> generateFor(ProfileId profileId, SensoryTrigger trigger, Instant generatedAt) {
        return Arrays.stream(RecommendationTopic.values())
                .map(topic -> new Recommendation(profileId, topic, titleFor(topic), contentFor(topic, trigger), generatedAt))
                .toList();
    }

    public static String titleFor(RecommendationTopic topic) {
        return switch (topic) {
            case SENSORY -> "Cuidar el entorno sensorial";
            case COMMUNICATIONAL -> "Facilitar la comunicación durante la crisis";
            case BEHAVIORAL -> "Mantener rutinas y anticipar cambios";
        };
    }

    public static String contentFor(RecommendationTopic topic, SensoryTrigger trigger) {
        var resolved = trigger == null ? SensoryTrigger.NONE : trigger;
        return switch (topic) {
            case SENSORY -> "Observa qué estímulos del entorno aumentan la tensión y redúcelos antes de intervenir."
                    + sensoryAdvice(resolved);
            case COMMUNICATIONAL -> "Durante una crisis el habla puede volverse difícil: ofrece el tablero de pictogramas, "
                    + "usa frases cortas y espera la respuesta sin apurar.";
            case BEHAVIORAL -> "Mantén una rutina predecible y anticipa los cambios. Después del episodio, anota qué lo "
                    + "detonó y qué ayudó para ajustar la próxima intervención."
                    + (resolved == SensoryTrigger.ROUTINE_CHANGE
                    ? " Los cambios de rutina fueron un detonante: avísalos con anticipación y con apoyo visual."
                    : "");
        };
    }

    private static String sensoryAdvice(SensoryTrigger trigger) {
        return switch (trigger) {
            case AUDITORY -> " El usuario es sensible a los sonidos: usa un tono de voz bajo, evita ruidos repentinos "
                    + "y ofrece audífonos con cancelación de ruido.";
            case VISUAL -> " El usuario es sensible a lo visual: baja las luces, evita destellos y mantén el espacio ordenado.";
            case TACTILE -> " El usuario es sensible al contacto: avisa antes de tocarlo y ofrece un objeto sensorial, "
                    + "como un mordedor.";
            case CROWD -> " Las multitudes fueron un detonante: busca un lugar apartado y tranquilo antes de que la tensión aumente.";
            case ROUTINE_CHANGE, OTHER -> "";
            case NONE -> " Completa el perfil sensorial del usuario para recibir recomendaciones más precisas.";
        };
    }
}
