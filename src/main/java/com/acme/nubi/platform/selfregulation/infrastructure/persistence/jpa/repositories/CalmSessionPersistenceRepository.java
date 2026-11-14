package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities.CalmSessionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalmSessionPersistenceRepository extends JpaRepository<CalmSessionPersistenceEntity, Long> {
    List<CalmSessionPersistenceEntity> findAllByProfileIdOrderByStartedAtDescIdDesc(Long profileId);

    Optional<CalmSessionPersistenceEntity> findFirstByProfileIdOrderByStartedAtDescIdDesc(Long profileId);
}
