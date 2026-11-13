package com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform;

import com.acme.nubi.platform.assistivecomunication.domain.model.commands.CreatePictogramCommand;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.CreatePictogramResource;

public class CreatePictogramCommandFromResourceAssembler {
    private CreatePictogramCommandFromResourceAssembler() {
    }

    public static CreatePictogramCommand toCommandFromResource(Long profileId, CreatePictogramResource resource) {
        return new CreatePictogramCommand(profileId, resource.label(), resource.phrase(), resource.imageUrl(),
                resource.audioUrl(), resource.categoryId());
    }
}
