package com.acme.nubi.platform.selfregulation.interfaces.rest.transform;

import com.acme.nubi.platform.selfregulation.domain.model.entities.FavoriteResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.FavoriteResourceResource;

public final class FavoriteResourceResourceFromEntityAssembler {

    private FavoriteResourceResourceFromEntityAssembler() {
    }

    public static FavoriteResourceResource toResourceFromEntity(FavoriteResource entity) {
        return new FavoriteResourceResource(
                entity.getId(),
                entity.getProfileId().value(),
                entity.getResourceId(),
                entity.getMarkedAt());
    }
}
