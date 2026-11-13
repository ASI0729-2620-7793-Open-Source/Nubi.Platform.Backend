package com.acme.nubi.platform.supportnetwork.application.internal.queryservices;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.EpisodeRecord;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetAllEpisodeRecordsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetEpisodeRecordByIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetEpisodeRecordsByProfileIdAndDateRangeQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetRecentEpisodeRecordsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories.EpisodeRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EpisodeRecordQueryServiceImpl {

    private static final int DEFAULT_RECENT_EPISODE_LIMIT = 5;

    private final EpisodeRecordRepository episodeRecordRepository;

    public EpisodeRecordQueryServiceImpl(EpisodeRecordRepository episodeRecordRepository) {
        this.episodeRecordRepository = episodeRecordRepository;
    }

    public Optional<EpisodeRecord> handle(GetEpisodeRecordByIdQuery query) {
        return episodeRecordRepository.findById(query.episodeRecordId());
    }

    public List<EpisodeRecord> handle(GetAllEpisodeRecordsByProfileIdQuery query) {
        return episodeRecordRepository.findAllByProfileId(query.profileId());
    }

    public List<EpisodeRecord> handle(GetEpisodeRecordsByProfileIdAndDateRangeQuery query) {
        return episodeRecordRepository.findAllByProfileIdAndStartedAtBetween(
                query.profileId(),
                query.startDate(),
                query.endDate());
    }

    public List<EpisodeRecord> handle(GetRecentEpisodeRecordsByProfileIdQuery query) {
        int limit = query.limit() == null || query.limit() <= 0
                ? DEFAULT_RECENT_EPISODE_LIMIT
                : query.limit();
        return episodeRecordRepository.findAllByProfileIdOrderByStartedAtDesc(query.profileId())
                .stream()
                .limit(limit)
                .toList();
    }
}
