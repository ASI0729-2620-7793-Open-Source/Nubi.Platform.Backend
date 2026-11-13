package com.acme.nubi.platform.selfregulation.interfaces.rest.resources;

public record CalmingResourceResource(
        Long id,
        String code,
        String name,
        String type,
        String mediaUrl,
        int maxIntensity,
        boolean availableOffline,
        boolean active) {
}
