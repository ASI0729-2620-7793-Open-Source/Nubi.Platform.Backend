package com.acme.nubi.platform.supportnetwork.application.internal.commandservices;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateRecommendationCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.MarkRecommendationAsEffectiveCommand;
import com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories.RecommendationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("supportNetworkRecommendationCommandServiceImpl")
@Transactional
public class RecommendationCommandServiceImpl {

    private final RecommendationRepository recommendationRepository;

    public RecommendationCommandServiceImpl(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    public Recommendation handle(CreateRecommendationCommand command) {
        return recommendationRepository.save(Recommendation.create(command));
    }

    public Recommendation handle(MarkRecommendationAsEffectiveCommand command) {
        Recommendation recommendation = recommendationRepository.findById(command.recommendationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recommendation not found: " + command.recommendationId()));
        recommendation.markAsEffective(command);
        return recommendationRepository.save(recommendation);
    }
}
