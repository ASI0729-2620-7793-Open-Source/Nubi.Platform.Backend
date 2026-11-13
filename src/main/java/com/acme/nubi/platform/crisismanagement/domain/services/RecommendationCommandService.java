package com.acme.nubi.platform.crisismanagement.domain.services;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.GenerateRecommendationsCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.RatePreferenceCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.RefreshRecommendationsCommand;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;

import java.util.List;

public interface RecommendationCommandService {
    Result<List<Recommendation>, DomainError> handle(GenerateRecommendationsCommand command);

    Result<Recommendation, DomainError> handle(RatePreferenceCommand command);

    Result<List<Recommendation>, DomainError> handle(RefreshRecommendationsCommand command);
}
