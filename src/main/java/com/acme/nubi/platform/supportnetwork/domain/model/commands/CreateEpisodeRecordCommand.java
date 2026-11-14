package com.acme.nubi.platform.supportnetwork.domain.model.commands;

import java.time.LocalDateTime;

public record CreateEpisodeRecordCommand(
        Long profileId,
        Long sosSessionId,
        String emotionalState,
        String trigger,
        String strategyUsed,
        String notes,
        LocalDateTime startedAt,
        LocalDateTime endedAt) {
}
