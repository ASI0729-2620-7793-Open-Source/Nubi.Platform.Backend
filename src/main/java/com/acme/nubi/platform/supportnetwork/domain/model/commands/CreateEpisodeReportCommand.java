package com.acme.nubi.platform.supportnetwork.domain.model.commands;

import java.time.LocalDateTime;

public record CreateEpisodeReportCommand(
        Long profileId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        Integer episodeCount,
        String summary,
        String observations) {
}
