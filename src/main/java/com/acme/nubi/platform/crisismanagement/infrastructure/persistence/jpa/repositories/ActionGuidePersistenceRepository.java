package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.ActionGuidePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActionGuidePersistenceRepository extends JpaRepository<ActionGuidePersistenceEntity, Long> {
    Optional<ActionGuidePersistenceEntity> findFirstByActiveTrueOrderByVersionDescIdDesc();
}
