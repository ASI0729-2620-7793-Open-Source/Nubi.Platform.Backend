package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.mappers;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmSession;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.CalmTimer;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities.CalmSessionPersistenceEntity;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities.CalmTimerPersistenceEmbeddable;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities.CalmingResourcePersistenceEntity;

public final class CalmSessionPersistenceMapper {

    private CalmSessionPersistenceMapper() {
    }

    public static CalmSession toDomain(CalmSessionPersistenceEntity entity) {
        var resource = entity.getResource();
        return new CalmSession(
                entity.getId(),
                new ProfileId(entity.getProfileId()),
                resource == null ? null : resource.getId(),
                entity.getStatus(),
                entity.getIntensity(),
                entity.isLowStimulationActive(),
                entity.isStartedBySos(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                toDomainTimer(entity.getTimer()));
    }

    /**
     * @param resource referencia JPA al recurso de la sesión, o {@code null} si no tiene recurso
     */
    public static CalmSessionPersistenceEntity toPersistence(CalmSession session,
                                                             CalmingResourcePersistenceEntity resource) {
        var entity = new CalmSessionPersistenceEntity();
        entity.setId(session.getId());
        entity.setProfileId(session.getProfileId().value());
        entity.setResource(resource);
        entity.setStatus(session.getStatus());
        entity.setIntensity(session.getIntensity());
        entity.setLowStimulationActive(session.isLowStimulationActive());
        entity.setStartedBySos(session.isStartedBySos());
        entity.setTimer(toPersistenceTimer(session.getTimer()));
        entity.setStartedAt(session.getStartedAt());
        entity.setEndedAt(session.getEndedAt());
        return entity;
    }

    private static CalmTimer toDomainTimer(CalmTimerPersistenceEmbeddable timer) {
        if (timer == null || timer.getDurationMinutes() == null || timer.getRemainingSeconds() == null) {
            return null;
        }
        return new CalmTimer(timer.getDurationMinutes(), timer.getRemainingSeconds(), timer.isRunning());
    }

    private static CalmTimerPersistenceEmbeddable toPersistenceTimer(CalmTimer timer) {
        if (timer == null) {
            return CalmTimerPersistenceEmbeddable.empty();
        }
        return new CalmTimerPersistenceEmbeddable(timer.durationMinutes(), timer.remainingSeconds(), timer.running());
    }
}
