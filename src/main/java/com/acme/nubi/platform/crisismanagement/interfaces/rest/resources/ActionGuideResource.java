package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

import java.util.List;

public record ActionGuideResource(
        Long id,
        String name,
        String description,
        int version,
        boolean active,
        int totalSteps,
        List<ContainmentStepResource> steps) {
}
