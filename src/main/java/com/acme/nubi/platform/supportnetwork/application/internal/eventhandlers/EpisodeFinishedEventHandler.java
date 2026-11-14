package com.acme.nubi.platform.supportnetwork.application.internal.eventhandlers;

import com.acme.nubi.platform.crisismanagement.domain.model.events.EpisodeFinishedEvent;
import com.acme.nubi.platform.crisismanagement.domain.model.events.EpisodeRegisteredEvent;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.supportnetwork.application.internal.commandservices.EpisodeRecordCommandServiceImpl;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateEpisodeRecordCommand;
import java.time.Clock;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Cuando termina una sesión del Modo SOS, Red de Apoyo y Seguimiento registra el episodio en el
 * historial del niño sin que el cuidador tenga que volver a escribirlo. Después avisa con
 * {@link EpisodeRegisteredEvent} para que Gestión de Crisis actualice sus recomendaciones.
 */
@Service("supportNetworkEpisodeFinishedEventHandler")
public class EpisodeFinishedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpisodeFinishedEventHandler.class);

    private final EpisodeRecordCommandServiceImpl episodeRecordCommandService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public EpisodeFinishedEventHandler(EpisodeRecordCommandServiceImpl episodeRecordCommandService,
                                       ApplicationEventPublisher eventPublisher,
                                       Clock clock) {
        this.episodeRecordCommandService = episodeRecordCommandService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @EventListener
    public void on(EpisodeFinishedEvent event) {
        // Red de Apoyo guarda fechas locales (LocalDateTime) igual que createdAt del episodio
        var endedAt = LocalDateTime.now(clock.withZone(java.time.ZoneId.systemDefault()));
        var startedAt = endedAt.minusMinutes(Math.max(event.durationMinutes(), 0));
        var finalIntensity = event.finalIntensity() == null ? event.initialIntensity() : event.finalIntensity();
        var strategy = "Guía de contención del Modo SOS (" + event.completedSteps() + " de "
                + event.totalSteps() + " pasos)";
        var notes = "Registro automático. Intensidad inicial: " + event.initialIntensity()
                + "; intensidad final: " + finalIntensity + ".";

        var record = episodeRecordCommandService.handle(new CreateEpisodeRecordCommand(
                event.profileId(),
                event.sessionId(),
                finalIntensity == null ? null : finalIntensity.name(),
                event.trigger() == null ? null : event.trigger().name(),
                strategy,
                notes,
                startedAt,
                endedAt));
        LOGGER.info("Episode record {} registered from SOS session {}", record.getId(), event.sessionId());

        // Un episodio es relevante para las recomendaciones si el niño no terminó en calma.
        var relevant = finalIntensity != EmotionalIntensityLevel.CALM;
        eventPublisher.publishEvent(
                new EpisodeRegisteredEvent(record.getId(), event.profileId(), event.trigger(), relevant));
    }
}
