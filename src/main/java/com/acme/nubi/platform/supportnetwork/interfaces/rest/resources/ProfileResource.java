package com.acme.nubi.platform.supportnetwork.interfaces.rest.resources;

public record ProfileResource(
        Long id,
        String name,
        String avatarUrl,
        String currentEmotionalState) {
}
