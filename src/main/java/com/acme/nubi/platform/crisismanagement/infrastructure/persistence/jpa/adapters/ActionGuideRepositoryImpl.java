package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.ActionGuide;
import com.acme.nubi.platform.crisismanagement.domain.repositories.ActionGuideRepository;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.mappers.ActionGuidePersistenceMapper;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.repositories.ActionGuidePersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ActionGuideRepositoryImpl implements ActionGuideRepository {

    private final ActionGuidePersistenceRepository actionGuidePersistenceRepository;

    public ActionGuideRepositoryImpl(ActionGuidePersistenceRepository actionGuidePersistenceRepository) {
        this.actionGuidePersistenceRepository = actionGuidePersistenceRepository;
    }

    @Override
    public ActionGuide save(ActionGuide guide) {
        var saved = actionGuidePersistenceRepository.saveAndFlush(ActionGuidePersistenceMapper.toPersistence(guide));
        return ActionGuidePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<ActionGuide> findById(Long id) {
        return actionGuidePersistenceRepository.findById(id).map(ActionGuidePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return actionGuidePersistenceRepository.existsById(id);
    }

    @Override
    public Optional<ActionGuide> findActiveGuide() {
        return actionGuidePersistenceRepository.findFirstByActiveTrueOrderByVersionDescIdDesc()
                .map(ActionGuidePersistenceMapper::toDomain);
    }
}
