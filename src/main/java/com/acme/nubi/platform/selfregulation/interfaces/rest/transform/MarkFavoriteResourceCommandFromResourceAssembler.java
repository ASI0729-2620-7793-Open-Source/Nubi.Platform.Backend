package com.acme.nubi.platform.selfregulation.interfaces.rest.transform;

import com.acme.nubi.platform.selfregulation.domain.model.commands.MarkFavoriteResourceCommand;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.MarkFavoriteResourceResource;

public final class MarkFavoriteResourceCommandFromResourceAssembler {

    private MarkFavoriteResourceCommandFromResourceAssembler() {
    }

    public static MarkFavoriteResourceCommand toCommandFromResource(Long profileId,
                                                                    MarkFavoriteResourceResource resource) {
        return new MarkFavoriteResourceCommand(profileId, resource.resourceId());
    }
}
