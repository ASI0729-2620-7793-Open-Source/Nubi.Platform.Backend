package com.acme.nubi.platform.crisismanagement.interfaces.rest.transform;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.ActionGuide;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.AlternativeTechnique;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.ContainmentStep;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.ActionGuideResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.AlternativeTechniqueResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.ContainmentStepResource;

import java.util.List;

public final class ActionGuideResourceFromEntityAssembler {

    private ActionGuideResourceFromEntityAssembler() {
    }

    /** Guía con los pasos en su orden original. */
    public static ActionGuideResource toResourceFromEntity(ActionGuide guide) {
        return toResourceFromEntity(guide, guide.getSteps());
    }

    /** Guía con los pasos en el orden personalizado de una sesión. */
    public static ActionGuideResource toResourceFromEntity(ActionGuide guide, List<ContainmentStep> orderedSteps) {
        return new ActionGuideResource(
                guide.getId(),
                guide.getName(),
                guide.getDescription(),
                guide.getVersion(),
                guide.isActive(),
                guide.totalSteps(),
                orderedSteps.stream().map(step -> toStepResource(guide, step)).toList());
    }

    public static AlternativeTechniqueResource toTechniqueResource(Long stepId, AlternativeTechnique technique) {
        return new AlternativeTechniqueResource(
                technique.getId(), stepId, technique.getCode(), technique.getTitle(), technique.getDescription());
    }

    private static ContainmentStepResource toStepResource(ActionGuide guide, ContainmentStep step) {
        return new ContainmentStepResource(
                step.getId(),
                guide.getId(),
                step.getStepOrder(),
                step.getCode(),
                step.getTitle(),
                step.getDescription(),
                step.getMediaUrl(),
                step.isMandatory(),
                step.getRelatedTrigger().name(),
                step.getAlternativeTechniques().stream()
                        .map(technique -> toTechniqueResource(step.getId(), technique))
                        .toList());
    }
}
