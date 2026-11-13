package com.acme.nubi.platform.selfregulation.interfaces.rest.transform;

import com.acme.nubi.platform.selfregulation.domain.model.commands.StartCalmSessionCommand;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.StartCalmSessionResource;

public final class StartCalmSessionCommandFromResourceAssembler {

    private StartCalmSessionCommandFromResourceAssembler() {
    }

    public static StartCalmSessionCommand toCommandFromResource(StartCalmSessionResource resource) {
        return new StartCalmSessionCommand(
                resource.profileId(),
                resource.resourceId(),
                Boolean.TRUE.equals(resource.startedBySos()));
    }
}
