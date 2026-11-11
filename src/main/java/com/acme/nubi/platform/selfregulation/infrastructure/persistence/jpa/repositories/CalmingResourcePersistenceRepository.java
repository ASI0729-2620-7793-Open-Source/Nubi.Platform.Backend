package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ResourceType;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities.CalmingResourcePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalmingResourcePersistenceRepository extends JpaRepository<CalmingResourcePersistenceEntity, Long> {
    List<CalmingResourcePersistenceEntity> findAllByActiveTrueOrderByIdAsc();

    List<CalmingResourcePersistenceEntity> findAllByActiveTrueAndTypeOrderByIdAsc(ResourceType type);

    boolean existsByCode(String code);
}
