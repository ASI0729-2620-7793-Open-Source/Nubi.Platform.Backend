package com.acme.nubi.platform.supportnetwork.interfaces.rest.transform;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.EpisodeRecord;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.EpisodeRecordResource;

public final class EpisodeRecordResourceFromEntityAssembler {

    private EpisodeRecordResourceFromEntityAssembler() {
    }

    public static EpisodeRecordResource toResourceFromEntity(EpisodeRecord entity) {
        return new EpisodeRecordResource(
                entity.getId(),
                entity.getProfileId(),
                entity.getSosSessionId(),
                entity.getEmotionalState(),
                entity.getTrigger(),
                entity.getStrategyUsed(),
                entity.getNotes(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                entity.getCreatedAt());
    }
}
