package com.acme.nubi.platform.visualroutine.domain.repositories;

import com.acme.nubi.platform.visualroutine.domain.model.aggregates.RoutineStep;
import java.util.List;
import java.util.Optional;

public interface RoutineStepRepository {

    RoutineStep save(RoutineStep step);

    Optional<RoutineStep> findById(Long id);

    /** Pasos del perfil ordenados por posición. */
    List<RoutineStep> findByProfileId(Long profileId);

    void delete(RoutineStep step);
}
