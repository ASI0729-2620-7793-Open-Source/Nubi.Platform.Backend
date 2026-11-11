package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.mappers;

import com.acme.nubi.platform.selfregulation.domain.model.entities.FavoriteResource;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities.CalmingResourcePersistenceEntity;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities.FavoriteResourcePersistenceEntity;

public final class FavoriteResourcePersistenceMapper {

    private FavoriteResourcePersistenceMapper() {
    }

    public static FavoriteResource toDomain(FavoriteResourcePersistenceEntity entity) {
        return new FavoriteResource(
                entity.getId(),
                new ProfileId(entity.getProfileId()),
                entity.getResource().getId(),
                entity.getMarkedAt());
    }

    public static FavoriteResourcePersistenceEntity toPersistence(FavoriteResource favorite,
                                                                  CalmingResourcePersistenceEntity resource) {
        var entity = new FavoriteResourcePersistenceEntity();
        entity.setId(favorite.getId());
        entity.setProfileId(favorite.getProfileId().value());
        entity.setResource(resource);
        entity.setMarkedAt(favorite.getMarkedAt());
        return entity;
    }
}
