package com.acme.nubi.platform.crisismanagement.domain.model.commands;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;

/**
 * Actualiza las recomendaciones de un perfil a partir de un episodio registrado.
 *
 * @param relevant si el episodio aporta información nueva; si no, no se toca nada
 */
public record RefreshRecommendationsCommand(Long profileId, SensoryTrigger trigger, boolean relevant) {

    public RefreshRecommendationsCommand {
        if (profileId == null || profileId <= 0) {
            throw new IllegalArgumentException("The profile id must be a positive number.");
        }
    }
}
