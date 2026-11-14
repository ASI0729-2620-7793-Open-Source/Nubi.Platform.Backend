package com.acme.nubi.platform.crisismanagement.application.internal.eventhandlers;

import com.acme.nubi.platform.crisismanagement.domain.model.commands.RefreshRecommendationsCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.events.EpisodeRegisteredEvent;
import com.acme.nubi.platform.crisismanagement.domain.services.RecommendationCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Cuando Red de Apoyo y Seguimiento registra un episodio, actualiza las recomendaciones del perfil.
 */
@Service("crisisManagementEpisodeRegisteredEventHandler")
public class EpisodeRegisteredEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpisodeRegisteredEventHandler.class);

    private final RecommendationCommandService recommendationCommandService;

    public EpisodeRegisteredEventHandler(RecommendationCommandService recommendationCommandService) {
        this.recommendationCommandService = recommendationCommandService;
    }

    @EventListener
    public void on(EpisodeRegisteredEvent event) {
        recommendationCommandService
                .handle(new RefreshRecommendationsCommand(event.profileId(), event.trigger(), event.relevant()))
                .fold(
                        recommendations -> {
                            LOGGER.info("Recommendations of profile {} refreshed after episode {}",
                                    event.profileId(), event.episodeId());
                            return null;
                        },
                        error -> {
                            LOGGER.warn("Could not refresh recommendations of profile {}: {}",
                                    event.profileId(), error.message());
                            return null;
                        });
    }
}
