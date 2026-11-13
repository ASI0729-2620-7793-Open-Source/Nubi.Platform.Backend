package com.acme.nubi.platform.supportnetwork.interfaces.rest.resources;

public record CreateRecommendationResource(
        Long profileId,
        Long episodeRecordId,
        String title,
        String description,
        String strategy,
        String actionGuide) {
}
