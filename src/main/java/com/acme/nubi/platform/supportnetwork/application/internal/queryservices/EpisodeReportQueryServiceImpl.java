package com.acme.nubi.platform.supportnetwork.application.internal.queryservices;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.EpisodeReport;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetAllEpisodeReportsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetEpisodeReportByIdQuery;
import com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories.EpisodeReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EpisodeReportQueryServiceImpl {

    private final EpisodeReportRepository episodeReportRepository;

    public EpisodeReportQueryServiceImpl(EpisodeReportRepository episodeReportRepository) {
        this.episodeReportRepository = episodeReportRepository;
    }

    public Optional<EpisodeReport> handle(GetEpisodeReportByIdQuery query) {
        return episodeReportRepository.findById(query.episodeReportId());
    }

    public List<EpisodeReport> handle(GetAllEpisodeReportsByProfileIdQuery query) {
        return episodeReportRepository.findAllByProfileId(query.profileId());
    }
}
