package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.mappers;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmingResource;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities.CalmingResourcePersistenceEntity;

public final class CalmingResourcePersistenceMapper {

    private CalmingResourcePersistenceMapper() {
    }

    public static CalmingResource toDomain(CalmingResourcePersistenceEntity entity) {
        return new CalmingResource(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getType(),
                entity.getMediaUrl(),
                entity.getMaxIntensity(),
                entity.isAvailableOffline(),
                entity.isActive());
    }

    public static CalmingResourcePersistenceEntity toPersistence(CalmingResource resource) {
        var entity = new CalmingResourcePersistenceEntity();
        entity.setId(resource.getId());
        entity.setCode(resource.getCode());
        entity.setName(resource.getName());
        entity.setType(resource.getType());
        entity.setMediaUrl(resource.getMediaUrl());
        entity.setMaxIntensity(resource.getMaxIntensity());
        entity.setAvailableOffline(resource.isAvailableOffline());
        entity.setActive(resource.isActive());
        return entity;
    }
}
