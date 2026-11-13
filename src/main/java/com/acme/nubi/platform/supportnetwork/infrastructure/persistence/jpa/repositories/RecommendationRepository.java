package com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findAllByProfileId(Long profileId);

    List<Recommendation> findAllByProfileIdAndStatus(Long profileId, String status);
}
