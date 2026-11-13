package com.acme.nubi.platform.supportnetwork.domain.model.queries;

import java.time.LocalDateTime;

public record GetEpisodeRecordsByProfileIdAndDateRangeQuery(
        Long profileId,
        LocalDateTime startDate,
        LocalDateTime endDate) {
}
