package com.acme.nubi.platform.supportnetwork.interfaces.rest.resources;

import java.time.LocalDateTime;

public record EpisodeRecordResource(
        Long id,
        Long profileId,
        Long sosSessionId,
        String emotionalState,
        String trigger,
        String strategyUsed,
        String notes,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime createdAt) {
}
