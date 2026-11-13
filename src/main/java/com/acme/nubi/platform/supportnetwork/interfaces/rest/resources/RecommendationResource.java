package com.acme.nubi.platform.supportnetwork.interfaces.rest.resources;

import java.time.LocalDateTime;

public record RecommendationResource(
        Long id,
        Long profileId,
        Long episodeRecordId,
        String title,
        String description,
        String strategy,
        String actionGuide,
        String status,
        Boolean effective,
        LocalDateTime createdAt,
        LocalDateTime markedEffectiveAt) {
}
