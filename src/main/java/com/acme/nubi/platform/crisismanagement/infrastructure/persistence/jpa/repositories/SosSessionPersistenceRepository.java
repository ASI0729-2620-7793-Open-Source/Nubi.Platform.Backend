package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SosSessionStatus;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.SosSessionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SosSessionPersistenceRepository extends JpaRepository<SosSessionPersistenceEntity, Long> {
    Optional<SosSessionPersistenceEntity> findFirstByProfileIdAndStatusOrderByStartedAtDescIdDesc(
            Long profileId, SosSessionStatus status);

    List<SosSessionPersistenceEntity> findAllByProfileIdOrderByStartedAtDescIdDesc(Long profileId);
}
