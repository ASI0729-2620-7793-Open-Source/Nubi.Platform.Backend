package com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.EpisodeReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeReportRepository extends JpaRepository<EpisodeReport, Long> {

    List<EpisodeReport> findAllByProfileId(Long profileId);
}
