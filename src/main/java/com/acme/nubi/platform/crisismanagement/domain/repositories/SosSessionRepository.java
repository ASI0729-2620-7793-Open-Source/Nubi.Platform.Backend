package com.acme.nubi.platform.crisismanagement.domain.repositories;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.shared.domain.repositories.ICrudRepository;

import java.util.List;
import java.util.Optional;

public interface SosSessionRepository extends ICrudRepository<SosSession, Long> {
    /** Sesión en curso del perfil, si la hay. */
    Optional<SosSession> findActiveByProfileId(ProfileId profileId);

    /** Sesiones del perfil, de la más reciente a la más antigua. */
    List<SosSession> findAllByProfileId(ProfileId profileId);
}
