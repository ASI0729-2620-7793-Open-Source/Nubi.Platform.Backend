package com.acme.nubi.platform.supportnetwork.interfaces.rest.transform;

import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateEpisodeReportCommand;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.CreateEpisodeReportResource;

public final class CreateEpisodeReportCommandFromResourceAssembler {

    private CreateEpisodeReportCommandFromResourceAssembler() {
    }

    public static CreateEpisodeReportCommand toCommandFromResource(CreateEpisodeReportResource resource) {
        return new CreateEpisodeReportCommand(
                resource.profileId(),
                resource.periodStart(),
                resource.periodEnd(),
                resource.episodeCount(),
                resource.summary(),
                resource.observations());
    }
}
