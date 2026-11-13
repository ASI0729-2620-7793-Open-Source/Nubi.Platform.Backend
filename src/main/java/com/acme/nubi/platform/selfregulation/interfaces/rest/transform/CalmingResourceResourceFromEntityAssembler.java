package com.acme.nubi.platform.selfregulation.interfaces.rest.transform;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmingResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.CalmingResourceResource;

public final class CalmingResourceResourceFromEntityAssembler {

    private CalmingResourceResourceFromEntityAssembler() {
    }

    public static CalmingResourceResource toResourceFromEntity(CalmingResource entity) {
        return new CalmingResourceResource(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getType().name(),
                entity.getMediaUrl(),
                entity.getMaxIntensity(),
                entity.isAvailableOffline(),
                entity.isActive());
    }
}
