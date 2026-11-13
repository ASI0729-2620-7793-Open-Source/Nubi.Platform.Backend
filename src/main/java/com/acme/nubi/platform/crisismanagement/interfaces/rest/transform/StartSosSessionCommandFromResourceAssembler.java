package com.acme.nubi.platform.crisismanagement.interfaces.rest.transform;

import com.acme.nubi.platform.crisismanagement.domain.model.commands.StartSosSessionCommand;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.StartSosSessionResource;

public final class StartSosSessionCommandFromResourceAssembler {

    private StartSosSessionCommandFromResourceAssembler() {
    }

    public static StartSosSessionCommand toCommandFromResource(StartSosSessionResource resource) {
        return new StartSosSessionCommand(
                resource.profileId(), resource.caregiverAccountId(), resource.trigger(), resource.initialIntensity());
    }
}
