package com.acme.nubi.platform.supportnetwork.interfaces.rest.transform;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.EpisodeReport;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.EpisodeReportResource;

public final class EpisodeReportResourceFromEntityAssembler {

    private EpisodeReportResourceFromEntityAssembler() {
    }

    public static EpisodeReportResource toResourceFromEntity(EpisodeReport entity) {
        return new EpisodeReportResource(
                entity.getId(),
                entity.getProfileId(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getEpisodeCount(),
                entity.getSummary(),
                entity.getObservations(),
                entity.getShared(),
                entity.getSharedWith(),
                entity.getCreatedAt(),
                entity.getSharedAt());
    }
}
