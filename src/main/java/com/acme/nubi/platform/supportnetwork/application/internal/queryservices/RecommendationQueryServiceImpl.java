package com.acme.nubi.platform.supportnetwork.application.internal.queryservices;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetActiveRecommendationsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetAllRecommendationsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetRecommendationByIdQuery;
import com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RecommendationQueryServiceImpl {

    private final RecommendationRepository recommendationRepository;

    public RecommendationQueryServiceImpl(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    public Optional<Recommendation> handle(GetRecommendationByIdQuery query) {
        return recommendationRepository.findById(query.recommendationId());
    }

    public List<Recommendation> handle(GetAllRecommendationsByProfileIdQuery query) {
        return recommendationRepository.findAllByProfileId(query.profileId());
    }

    public List<Recommendation> handle(GetActiveRecommendationsByProfileIdQuery query) {
        return recommendationRepository.findAllByProfileIdAndStatus(
                query.profileId(),
                Recommendation.ACTIVE);
    }
}
