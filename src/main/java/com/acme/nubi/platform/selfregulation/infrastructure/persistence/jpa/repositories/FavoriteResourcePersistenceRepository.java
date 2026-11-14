package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities.FavoriteResourcePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteResourcePersistenceRepository extends JpaRepository<FavoriteResourcePersistenceEntity, Long> {
    List<FavoriteResourcePersistenceEntity> findAllByProfileIdOrderByMarkedAtDesc(Long profileId);

    Optional<FavoriteResourcePersistenceEntity> findByProfileIdAndResource_Id(Long profileId, Long resourceId);

    boolean existsByProfileIdAndResource_Id(Long profileId, Long resourceId);
}
