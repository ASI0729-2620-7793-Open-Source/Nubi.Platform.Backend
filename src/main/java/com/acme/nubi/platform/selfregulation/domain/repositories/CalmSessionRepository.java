package com.acme.nubi.platform.selfregulation.domain.repositories;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmSession;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.shared.domain.repositories.ICrudRepository;

import java.util.List;
import java.util.Optional;

public interface CalmSessionRepository extends ICrudRepository<CalmSession, Long> {
    /** Sesiones del perfil, de la más reciente a la más antigua. */
    List<CalmSession> findAllByProfileId(ProfileId profileId);

    Optional<CalmSession> findLatestByProfileId(ProfileId profileId);
}
