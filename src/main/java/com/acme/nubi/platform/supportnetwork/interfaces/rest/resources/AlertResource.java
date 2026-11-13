package com.acme.nubi.platform.supportnetwork.interfaces.rest.resources;

import java.time.LocalDateTime;

public record AlertResource(
        Long id,
        Long profileId,
        String type,
        String message,
        String severity,
        LocalDateTime createdAt) {
}
