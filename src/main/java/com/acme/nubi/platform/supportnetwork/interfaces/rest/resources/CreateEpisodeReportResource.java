package com.acme.nubi.platform.supportnetwork.interfaces.rest.resources;

import java.time.LocalDateTime;

public record CreateEpisodeReportResource(
        Long profileId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        Integer episodeCount,
        String summary,
        String observations) {
}
