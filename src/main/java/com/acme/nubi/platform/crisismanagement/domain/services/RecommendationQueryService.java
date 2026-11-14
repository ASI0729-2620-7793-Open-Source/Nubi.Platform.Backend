package com.acme.nubi.platform.crisismanagement.domain.services;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetRecommendationByIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetRecommendationsByProfileIdQuery;

import java.util.List;
import java.util.Optional;

public interface RecommendationQueryService {
    Optional<Recommendation> handle(GetRecommendationByIdQuery query);

    List<Recommendation> handle(GetRecommendationsByProfileIdQuery query);
}
