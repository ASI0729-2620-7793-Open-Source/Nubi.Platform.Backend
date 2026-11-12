package com.acme.nubi.platform.crisismanagement.interfaces.rest.transform;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.RecommendationPreferenceResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.RecommendationResource;

public final class RecommendationResourceFromEntityAssembler {

    private RecommendationResourceFromEntityAssembler() {
    }

    public static RecommendationResource toResourceFromEntity(Recommendation entity) {
        return new RecommendationResource(
                entity.getId(),
                entity.getProfileId().value(),
                entity.getTopic().name(),
                entity.getTitle(),
                entity.getContent(),
                entity.getVersion(),
                entity.isOutdated(),
                entity.getGeneratedAt(),
                entity.getPreferences().stream()
                        .map(preference -> new RecommendationPreferenceResource(
                                preference.getAccountId().value(),
                                preference.getUsefulnessRating(),
                                preference.isFavorite()))
                        .toList());
    }
}
