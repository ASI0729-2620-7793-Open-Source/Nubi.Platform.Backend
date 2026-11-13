package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.mappers;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.CompletedStep;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.AccountId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.CompletedStepPersistenceEntity;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.SosSessionPersistenceEntity;

public final class SosSessionPersistenceMapper {

    private SosSessionPersistenceMapper() {
    }

    public static SosSession toDomain(SosSessionPersistenceEntity entity) {
        return new SosSession(
                entity.getId(),
                new ProfileId(entity.getProfileId()),
                new AccountId(entity.getCaregiverAccountId()),
                entity.getGuideId(),
                entity.getStatus(),
                entity.getCurrentStepOrder(),
                entity.getTotalSteps(),
                entity.getInitialIntensity(),
                entity.getFinalIntensity(),
                entity.getTrigger(),
                entity.getObservedState(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getCompletedSteps().stream().map(SosSessionPersistenceMapper::toDomainStep).toList());
    }

    /** Sesión nueva: copia todo el agregado. */
    public static SosSessionPersistenceEntity toPersistence(SosSession session) {
        var entity = new SosSessionPersistenceEntity();
        entity.setProfileId(session.getProfileId().value());
        entity.setCaregiverAccountId(session.getCaregiverAccountId().value());
        entity.setGuideId(session.getGuideId());
        entity.setInitialIntensity(session.getInitialIntensity());
        entity.setTrigger(session.getTrigger());
        entity.setTotalSteps(session.getTotalSteps());
        entity.setStartedAt(session.getStartedAt());
        apply(entity, session);
        return entity;
    }

    /**
     * Sincroniza una sesión ya guardada con el estado del agregado. Se actualiza la fila existente
     * en lugar de reemplazarla para conservar created_at, y solo se agregan los pasos nuevos.
     */
    public static void apply(SosSessionPersistenceEntity entity, SosSession session) {
        entity.setStatus(session.getStatus());
        entity.setCurrentStepOrder(session.getCurrentStepOrder());
        entity.setFinalIntensity(session.getFinalIntensity());
        entity.setObservedState(session.getObservedState());
        entity.setFinishedAt(session.getFinishedAt());
        for (var completed : session.getCompletedSteps()) {
            var alreadySaved = entity.getCompletedSteps().stream()
                    .anyMatch(saved -> saved.getStepId().equals(completed.getStepId()));
            if (!alreadySaved) {
                entity.getCompletedSteps().add(toPersistenceStep(completed, entity));
            }
        }
    }

    private static CompletedStep toDomainStep(CompletedStepPersistenceEntity entity) {
        return new CompletedStep(entity.getId(), entity.getStepId(), entity.isSkipped(), entity.getCompletedAt());
    }

    private static CompletedStepPersistenceEntity toPersistenceStep(CompletedStep completed,
                                                                    SosSessionPersistenceEntity session) {
        var entity = new CompletedStepPersistenceEntity();
        entity.setSession(session);
        entity.setStepId(completed.getStepId());
        entity.setSkipped(completed.isSkipped());
        entity.setCompletedAt(completed.getCompletedAt());
        return entity;
    }
}
