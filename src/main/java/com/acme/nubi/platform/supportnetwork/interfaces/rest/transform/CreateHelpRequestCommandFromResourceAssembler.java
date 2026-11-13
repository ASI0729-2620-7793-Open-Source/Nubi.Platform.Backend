package com.acme.nubi.platform.supportnetwork.interfaces.rest.transform;

import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateHelpRequestCommand;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.CreateHelpRequestResource;

public final class CreateHelpRequestCommandFromResourceAssembler {

    private CreateHelpRequestCommandFromResourceAssembler() {
    }

    public static CreateHelpRequestCommand toCommandFromResource(CreateHelpRequestResource resource) {
        return new CreateHelpRequestCommand(resource.profileId(), resource.message());
    }
}
