package com.acme.nubi.platform.crisismanagement.domain.model.queries;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.RecommendationTopic;

public record GetRecommendationsByProfileIdQuery(Long profileId, RecommendationTopic topic) {
}
