package com.acme.nubi.platform.crisismanagement.domain.repositories;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.ActionGuide;
import com.acme.nubi.platform.shared.domain.repositories.ICrudRepository;

import java.util.Optional;

public interface ActionGuideRepository extends ICrudRepository<ActionGuide, Long> {
    /** Guía vigente; si hay varias activas, la de versión más alta. */
    Optional<ActionGuide> findActiveGuide();
}
