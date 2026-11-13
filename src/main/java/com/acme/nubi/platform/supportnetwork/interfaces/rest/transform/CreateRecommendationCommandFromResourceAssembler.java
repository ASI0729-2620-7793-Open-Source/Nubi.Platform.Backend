package com.acme.nubi.platform.supportnetwork.interfaces.rest.transform;

import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateRecommendationCommand;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.CreateRecommendationResource;

public final class CreateRecommendationCommandFromResourceAssembler {

    private CreateRecommendationCommandFromResourceAssembler() {
    }

    public static CreateRecommendationCommand toCommandFromResource(CreateRecommendationResource resource) {
        return new CreateRecommendationCommand(
                resource.profileId(),
                resource.episodeRecordId(),
                resource.title(),
                resource.description(),
                resource.strategy(),
                resource.actionGuide());
    }
}
