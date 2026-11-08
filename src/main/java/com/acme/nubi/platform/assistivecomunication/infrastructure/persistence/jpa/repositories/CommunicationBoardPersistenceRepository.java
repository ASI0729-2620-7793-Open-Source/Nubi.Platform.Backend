package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.CommunicationBoardPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunicationBoardPersistenceRepository
        extends JpaRepository<CommunicationBoardPersistenceEntity, Long> {
    Optional<CommunicationBoardPersistenceEntity> findByProfileId(ProfileId profileId);

    boolean existsByProfileId(ProfileId profileId);
}
