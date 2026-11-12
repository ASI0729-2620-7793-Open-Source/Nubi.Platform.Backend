package com.acme.nubi.platform.crisismanagement.interfaces.rest.transform;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.CompletedStepResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.SosSessionResource;

public final class SosSessionResourceFromEntityAssembler {

    private SosSessionResourceFromEntityAssembler() {
    }

    public static SosSessionResource toResourceFromEntity(SosSession entity) {
        return new SosSessionResource(
                entity.getId(),
                entity.getProfileId().value(),
                entity.getCaregiverAccountId().value(),
                entity.getGuideId(),
                entity.getStatus().name(),
                entity.getCurrentStepOrder(),
                entity.getTotalSteps(),
                entity.getInitialIntensity().name(),
                entity.getFinalIntensity() == null ? null : entity.getFinalIntensity().name(),
                entity.getTrigger().name(),
                entity.getObservedState() == null ? null : entity.getObservedState().name(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.durationInMinutes(),
                entity.isLastStep(),
                entity.getCompletedSteps().stream()
                        .map(step -> new CompletedStepResource(step.getStepId(), step.isSkipped(), step.getCompletedAt()))
                        .toList());
    }
}
