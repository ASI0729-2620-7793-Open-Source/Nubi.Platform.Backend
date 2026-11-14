package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.selfregulation.domain.model.entities.FavoriteResource;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.domain.repositories.FavoriteResourceRepository;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.mappers.FavoriteResourcePersistenceMapper;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.repositories.CalmingResourcePersistenceRepository;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.repositories.FavoriteResourcePersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FavoriteResourceRepositoryImpl implements FavoriteResourceRepository {

    private final FavoriteResourcePersistenceRepository favoriteResourcePersistenceRepository;
    private final CalmingResourcePersistenceRepository calmingResourcePersistenceRepository;

    public FavoriteResourceRepositoryImpl(FavoriteResourcePersistenceRepository favoriteResourcePersistenceRepository,
                                          CalmingResourcePersistenceRepository calmingResourcePersistenceRepository) {
        this.favoriteResourcePersistenceRepository = favoriteResourcePersistenceRepository;
        this.calmingResourcePersistenceRepository = calmingResourcePersistenceRepository;
    }

    @Override
    public FavoriteResource save(FavoriteResource favorite) {
        var resource = calmingResourcePersistenceRepository.getReferenceById(favorite.getResourceId());
        var saved = favoriteResourcePersistenceRepository.save(
                FavoriteResourcePersistenceMapper.toPersistence(favorite, resource));
        return FavoriteResourcePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<FavoriteResource> findById(Long id) {
        return favoriteResourcePersistenceRepository.findById(id).map(FavoriteResourcePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return favoriteResourcePersistenceRepository.existsById(id);
    }

    @Override
    public List<FavoriteResource> findAllByProfileId(ProfileId profileId) {
        return favoriteResourcePersistenceRepository.findAllByProfileIdOrderByMarkedAtDesc(profileId.value()).stream()
                .map(FavoriteResourcePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<FavoriteResource> findByProfileIdAndResourceId(ProfileId profileId, Long resourceId) {
        return favoriteResourcePersistenceRepository.findByProfileIdAndResource_Id(profileId.value(), resourceId)
                .map(FavoriteResourcePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByProfileIdAndResourceId(ProfileId profileId, Long resourceId) {
        return favoriteResourcePersistenceRepository.existsByProfileIdAndResource_Id(profileId.value(), resourceId);
    }

    @Override
    public void delete(FavoriteResource favorite) {
        favoriteResourcePersistenceRepository.deleteById(favorite.getId());
    }
}
