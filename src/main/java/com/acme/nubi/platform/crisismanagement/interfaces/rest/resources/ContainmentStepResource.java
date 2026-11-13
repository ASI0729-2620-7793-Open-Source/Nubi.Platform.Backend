package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

import java.util.List;

public record ContainmentStepResource(
        Long id,
        Long guideId,
        int stepOrder,
        String code,
        String title,
        String description,
        String mediaUrl,
        boolean mandatory,
        String relatedTrigger,
        List<AlternativeTechniqueResource> alternativeTechniques) {
}
