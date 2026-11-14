package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

public record RecommendationPreferenceResource(Long accountId, Integer usefulnessRating, boolean favorite) {
}
