package com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.EpisodeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EpisodeRecordRepository extends JpaRepository<EpisodeRecord, Long> {

    List<EpisodeRecord> findAllByProfileId(Long profileId);

    List<EpisodeRecord> findAllByProfileIdOrderByStartedAtDesc(Long profileId);

    Optional<EpisodeRecord> findFirstByProfileIdOrderByStartedAtDesc(Long profileId);

    List<EpisodeRecord> findAllByProfileIdAndStartedAtBetween(
            Long profileId,
            LocalDateTime startDate,
            LocalDateTime endDate);
}
