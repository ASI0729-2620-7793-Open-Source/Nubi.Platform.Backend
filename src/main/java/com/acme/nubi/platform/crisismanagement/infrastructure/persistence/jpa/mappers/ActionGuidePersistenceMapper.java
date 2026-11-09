package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.mappers;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.ActionGuide;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.AlternativeTechnique;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.ContainmentStep;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.ActionGuidePersistenceEntity;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.AlternativeTechniquePersistenceEntity;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.ContainmentStepPersistenceEntity;

public final class ActionGuidePersistenceMapper {

    private ActionGuidePersistenceMapper() {
    }

    public static ActionGuide toDomain(ActionGuidePersistenceEntity entity) {
        return new ActionGuide(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getVersion(),
                entity.isActive(),
                entity.getSteps().stream().map(ActionGuidePersistenceMapper::toDomainStep).toList());
    }

    /** La guía es un dato maestro: solo se inserta (al sembrarla), nunca se modifica. */
    public static ActionGuidePersistenceEntity toPersistence(ActionGuide guide) {
        var entity = new ActionGuidePersistenceEntity();
        entity.setId(guide.getId());
        entity.setName(guide.getName());
        entity.setDescription(guide.getDescription());
        entity.setVersion(guide.getVersion());
        entity.setActive(guide.isActive());
        guide.getSteps().forEach(step -> entity.getSteps().add(toPersistenceStep(step, entity)));
        return entity;
    }

    private static ContainmentStep toDomainStep(ContainmentStepPersistenceEntity entity) {
        return new ContainmentStep(
                entity.getId(),
                entity.getStepOrder(),
                entity.getCode(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getMediaUrl(),
                entity.isMandatory(),
                entity.getRelatedTrigger(),
                entity.getAlternativeTechniques().stream()
                        .map(ActionGuidePersistenceMapper::toDomainTechnique)
                        .toList());
    }

    private static AlternativeTechnique toDomainTechnique(AlternativeTechniquePersistenceEntity entity) {
        return new AlternativeTechnique(entity.getId(), entity.getCode(), entity.getTitle(), entity.getDescription());
    }

    private static ContainmentStepPersistenceEntity toPersistenceStep(ContainmentStep step,
                                                                      ActionGuidePersistenceEntity guide) {
        var entity = new ContainmentStepPersistenceEntity();
        entity.setId(step.getId());
        entity.setGuide(guide);
        entity.setStepOrder(step.getStepOrder());
        entity.setCode(step.getCode());
        entity.setTitle(step.getTitle());
        entity.setDescription(step.getDescription());
        entity.setMediaUrl(step.getMediaUrl());
        entity.setMandatory(step.isMandatory());
        entity.setRelatedTrigger(step.getRelatedTrigger());
        step.getAlternativeTechniques()
                .forEach(technique -> entity.getAlternativeTechniques().add(toPersistenceTechnique(technique, entity)));
        return entity;
    }

    private static AlternativeTechniquePersistenceEntity toPersistenceTechnique(AlternativeTechnique technique,
                                                                                ContainmentStepPersistenceEntity step) {
        var entity = new AlternativeTechniquePersistenceEntity();
        entity.setId(technique.getId());
        entity.setStep(step);
        entity.setCode(technique.getCode());
        entity.setTitle(technique.getTitle());
        entity.setDescription(technique.getDescription());
        return entity;
    }
}
