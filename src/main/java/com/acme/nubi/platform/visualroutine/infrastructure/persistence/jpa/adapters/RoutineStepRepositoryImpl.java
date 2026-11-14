package com.acme.nubi.platform.visualroutine.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.visualroutine.domain.model.aggregates.RoutineStep;
import com.acme.nubi.platform.visualroutine.domain.repositories.RoutineStepRepository;
import com.acme.nubi.platform.visualroutine.infrastructure.persistence.jpa.entities.RoutineStepPersistenceEntity;
import com.acme.nubi.platform.visualroutine.infrastructure.persistence.jpa.repositories.RoutineStepPersistenceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RoutineStepRepositoryImpl implements RoutineStepRepository {

    private final RoutineStepPersistenceRepository persistenceRepository;

    public RoutineStepRepositoryImpl(RoutineStepPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public RoutineStep save(RoutineStep step) {
        var entity = step.getId() == null
                ? new RoutineStepPersistenceEntity()
                : persistenceRepository.findById(step.getId()).orElseGet(RoutineStepPersistenceEntity::new);
        entity.setProfileId(step.getProfileId());
        entity.setTitle(step.getTitle());
        entity.setIcon(step.getIcon());
        entity.setScheduledTime(step.getScheduledTime());
        entity.setPosition(step.getPosition());
        entity.setLastCompletedOn(step.getLastCompletedOn());
        return toDomain(persistenceRepository.save(entity));
    }

    @Override
    public Optional<RoutineStep> findById(Long id) {
        return persistenceRepository.findById(id).map(RoutineStepRepositoryImpl::toDomain);
    }

    @Override
    public List<RoutineStep> findByProfileId(Long profileId) {
        return persistenceRepository.findByProfileIdOrderByPositionAscIdAsc(profileId).stream()
                .map(RoutineStepRepositoryImpl::toDomain)
                .toList();
    }

    @Override
    public void delete(RoutineStep step) {
        persistenceRepository.deleteById(step.getId());
    }

    private static RoutineStep toDomain(RoutineStepPersistenceEntity entity) {
        return new RoutineStep(entity.getId(), entity.getProfileId(), entity.getTitle(), entity.getIcon(),
                entity.getScheduledTime(), entity.getPosition(), entity.getLastCompletedOn());
    }
}
