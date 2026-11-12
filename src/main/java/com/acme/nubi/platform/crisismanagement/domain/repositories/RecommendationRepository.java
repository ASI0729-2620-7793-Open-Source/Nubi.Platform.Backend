package com.acme.nubi.platform.crisismanagement.domain.repositories;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.RecommendationTopic;
import com.acme.nubi.platform.shared.domain.repositories.ICrudRepository;

import java.util.List;

public interface RecommendationRepository extends ICrudRepository<Recommendation, Long> {
    /** Recomendaciones vigentes del perfil (no desactualizadas), ordenadas por tópico. */
    List<Recommendation> findByProfileId(ProfileId profileId);

    List<Recommendation> findByProfileIdAndTopic(ProfileId profileId, RecommendationTopic topic);
}
