package com.acme.nubi.platform.crisismanagement.domain.model.commands;

/** Genera las recomendaciones de un perfil según su perfil sensorial. */
public record GenerateRecommendationsCommand(Long profileId) {

    public GenerateRecommendationsCommand {
        if (profileId == null || profileId <= 0) {
            throw new IllegalArgumentException("The profile id must be a positive number.");
        }
    }
}
