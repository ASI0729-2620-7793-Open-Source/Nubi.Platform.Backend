package com.acme.nubi.platform.supportnetwork.domain.model.commands;

public record CreateRecommendationCommand(
        Long profileId,
        Long episodeRecordId,
        String title,
        String description,
        String strategy,
        String actionGuide) {
}
