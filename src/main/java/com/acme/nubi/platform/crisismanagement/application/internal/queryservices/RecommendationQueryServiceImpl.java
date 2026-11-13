package com.acme.nubi.platform.crisismanagement.application.internal.queryservices;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetRecommendationByIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetRecommendationsByProfileIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.repositories.RecommendationRepository;
import com.acme.nubi.platform.crisismanagement.domain.services.RecommendationQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RecommendationQueryServiceImpl implements RecommendationQueryService {

    private final RecommendationRepository recommendationRepository;

    public RecommendationQueryServiceImpl(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public Optional<Recommendation> handle(GetRecommendationByIdQuery query) {
        return recommendationRepository.findById(query.recommendationId());
    }

    @Override
    public List<Recommendation> handle(GetRecommendationsByProfileIdQuery query) {
        var profileId = new ProfileId(query.profileId());
        return query.topic() == null
                ? recommendationRepository.findByProfileId(profileId)
                : recommendationRepository.findByProfileIdAndTopic(profileId, query.topic());
    }
}
