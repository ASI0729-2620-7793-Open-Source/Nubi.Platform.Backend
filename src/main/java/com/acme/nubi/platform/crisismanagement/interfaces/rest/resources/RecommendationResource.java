package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

public record RecommendationResource(
        Long id,
        Long profileId,
        String topic,
        String title,
        String content,
        int version,
        boolean outdated,
        Instant generatedAt,
        List<RecommendationPreferenceResource> preferences) {
}
