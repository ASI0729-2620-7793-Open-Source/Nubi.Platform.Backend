package com.acme.nubi.platform.supportnetwork.interfaces.rest.resources;

import java.time.LocalDateTime;

public record HelpRequestResource(
        Long id,
        Long profileId,
        String message,
        String status,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt) {
}
