package com.acme.nubi.platform.supportnetwork.interfaces.rest.transform;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.HelpRequest;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.HelpRequestResource;

public final class HelpRequestResourceFromEntityAssembler {

    private HelpRequestResourceFromEntityAssembler() {
    }

    public static HelpRequestResource toResourceFromEntity(HelpRequest entity) {
        return new HelpRequestResource(
                entity.getId(),
                entity.getProfileId(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getConfirmedAt(),
                entity.getCancelledAt());
    }
}
