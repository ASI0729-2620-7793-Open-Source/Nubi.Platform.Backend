package com.acme.nubi.platform.supportnetwork.interfaces.rest.transform;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.RecommendationResource;

public final class RecommendationResourceFromEntityAssembler {

    private RecommendationResourceFromEntityAssembler() {
    }

    public static RecommendationResource toResourceFromEntity(Recommendation entity) {
        return new RecommendationResource(
                entity.getId(),
                entity.getProfileId(),
                entity.getEpisodeRecordId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStrategy(),
                entity.getActionGuide(),
                entity.getStatus(),
                entity.getEffective(),
                entity.getCreatedAt(),
                entity.getMarkedEffectiveAt());
    }
}
