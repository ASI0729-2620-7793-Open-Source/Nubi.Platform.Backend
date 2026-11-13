package com.acme.nubi.platform.assistivecomunication.domain.repositories;

import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.CommunicationBoard;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.shared.domain.repositories.ICrudRepository;

import java.util.Optional;

public interface CommunicationBoardRepository extends ICrudRepository<CommunicationBoard, Long> {
    Optional<CommunicationBoard> findByProfileId(ProfileId profileId);

    boolean existsByProfileId(ProfileId profileId);
}
