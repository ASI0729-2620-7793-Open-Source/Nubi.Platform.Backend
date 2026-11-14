package com.acme.nubi.platform.supportnetwork.interfaces.rest.transform;

import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateEpisodeRecordCommand;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.CreateEpisodeRecordResource;

public final class CreateEpisodeRecordCommandFromResourceAssembler {

    private CreateEpisodeRecordCommandFromResourceAssembler() {
    }

    public static CreateEpisodeRecordCommand toCommandFromResource(CreateEpisodeRecordResource resource) {
        return new CreateEpisodeRecordCommand(
                resource.profileId(),
                resource.sosSessionId(),
                resource.emotionalState(),
                resource.trigger(),
                resource.strategyUsed(),
                resource.notes(),
                resource.startedAt(),
                resource.endedAt());
    }
}
