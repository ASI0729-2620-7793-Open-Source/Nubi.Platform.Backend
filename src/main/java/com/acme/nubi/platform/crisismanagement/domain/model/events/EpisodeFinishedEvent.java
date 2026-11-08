package com.acme.nubi.platform.crisismanagement.domain.model.events;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SosSessionStatus;
import com.acme.nubi.platform.shared.domain.model.events.DomainEvent;

/**
 * Se publica cuando termina una sesión SOS, completa o anticipadamente. Red de Apoyo y
 * Seguimiento lo consume para registrar el episodio sin que el cuidador lo documente a mano.
 *
 * @param completedSteps pasos atendidos, completados u omitidos
 */
public record EpisodeFinishedEvent(
        Long sessionId,
        Long profileId,
        SosSessionStatus status,
        int durationMinutes,
        int completedSteps,
        int totalSteps,
        SensoryTrigger trigger,
        EmotionalIntensityLevel initialIntensity,
        EmotionalIntensityLevel finalIntensity) implements DomainEvent {

    public static EpisodeFinishedEvent from(SosSession session) {
        return new EpisodeFinishedEvent(
                session.getId(),
                session.getProfileId().value(),
                session.getStatus(),
                session.durationInMinutes(),
                session.getCompletedSteps().size(),
                session.getTotalSteps(),
                session.getTrigger(),
                session.getInitialIntensity(),
                session.getFinalIntensity());
    }
}
