package com.acme.nubi.platform.visualroutine.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.visualroutine.infrastructure.persistence.jpa.entities.RoutineStepPersistenceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutineStepPersistenceRepository extends JpaRepository<RoutineStepPersistenceEntity, Long> {

    List<RoutineStepPersistenceEntity> findByProfileIdOrderByPositionAscIdAsc(Long profileId);
}
