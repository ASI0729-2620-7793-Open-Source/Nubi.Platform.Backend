package com.acme.nubi.platform.supportnetwork.interfaces.rest.resources;

import java.time.LocalDateTime;

public record EpisodeReportResource(
        Long id,
        Long profileId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        Integer episodeCount,
        String summary,
        String observations,
        Boolean shared,
        String sharedWith,
        LocalDateTime createdAt,
        LocalDateTime sharedAt) {
}
