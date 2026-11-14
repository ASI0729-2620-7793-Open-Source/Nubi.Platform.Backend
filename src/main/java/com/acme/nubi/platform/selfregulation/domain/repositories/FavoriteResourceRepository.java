package com.acme.nubi.platform.selfregulation.domain.repositories;

import com.acme.nubi.platform.selfregulation.domain.model.entities.FavoriteResource;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.shared.domain.repositories.ICrudRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteResourceRepository extends ICrudRepository<FavoriteResource, Long> {
    List<FavoriteResource> findAllByProfileId(ProfileId profileId);

    Optional<FavoriteResource> findByProfileIdAndResourceId(ProfileId profileId, Long resourceId);

    boolean existsByProfileIdAndResourceId(ProfileId profileId, Long resourceId);

    void delete(FavoriteResource favoriteResource);
}
