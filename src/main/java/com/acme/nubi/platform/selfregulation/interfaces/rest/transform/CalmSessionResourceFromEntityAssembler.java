package com.acme.nubi.platform.selfregulation.interfaces.rest.transform;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmSession;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.CalmSessionResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.CalmTimerResource;

public final class CalmSessionResourceFromEntityAssembler {

    private CalmSessionResourceFromEntityAssembler() {
    }

    public static CalmSessionResource toResourceFromEntity(CalmSession entity) {
        var timer = entity.getTimer() == null
                ? null
                : new CalmTimerResource(
                        entity.getTimer().durationMinutes(),
                        entity.getTimer().remainingSeconds(),
                        entity.getTimer().running());
        return new CalmSessionResource(
                entity.getId(),
                entity.getProfileId().value(),
                entity.getResourceId(),
                entity.getStatus().name(),
                entity.getIntensity(),
                entity.isLowStimulationActive(),
                entity.isStartedBySos(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                timer,
                entity.shouldSuggestHelp());
    }
}
