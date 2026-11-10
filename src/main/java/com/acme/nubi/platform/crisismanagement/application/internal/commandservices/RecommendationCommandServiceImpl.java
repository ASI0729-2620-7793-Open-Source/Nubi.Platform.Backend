package com.acme.nubi.platform.crisismanagement.application.internal.commandservices;

import com.acme.nubi.platform.crisismanagement.application.internal.outboundservices.acl.ExternalProfileService;
import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.GenerateRecommendationsCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.RatePreferenceCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.RefreshRecommendationsCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.errors.CrisisManagementErrors;
import com.acme.nubi.platform.crisismanagement.domain.model.policies.RecommendationGenerator;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.AccountId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryProfileSnapshot;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.crisismanagement.domain.repositories.RecommendationRepository;
import com.acme.nubi.platform.crisismanagement.domain.services.RecommendationCommandService;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class RecommendationCommandServiceImpl implements RecommendationCommandService {

    private final RecommendationRepository recommendationRepository;
    private final ExternalProfileService externalProfileService;
    private final Clock clock;

    public RecommendationCommandServiceImpl(RecommendationRepository recommendationRepository,
                                            ExternalProfileService externalProfileService,
                                            Clock clock) {
        this.recommendationRepository = recommendationRepository;
        this.externalProfileService = externalProfileService;
        this.clock = clock;
    }

    /** Genera las recomendaciones del perfil; si ya las tiene vigentes, las devuelve sin duplicarlas. */
    @Override
    public Result<List<Recommendation>, DomainError> handle(GenerateRecommendationsCommand command) {
        var profileId = new ProfileId(command.profileId());
        if (!externalProfileService.existsProfile(profileId)) {
            return Result.failure(CrisisManagementErrors.profileNotFound(command.profileId()));
        }
        var current = recommendationRepository.findByProfileId(profileId);
        if (!current.isEmpty()) {
            return Result.success(current);
        }
        return Result.success(generate(profileId, mostSensitiveTrigger(profileId)));
    }

    @Override
    public Result<Recommendation, DomainError> handle(RatePreferenceCommand command) {
        var found = recommendationRepository.findById(command.recommendationId());
        if (found.isEmpty()) {
            return Result.failure(CrisisManagementErrors.recommendationNotFound(command.recommendationId()));
        }
        var recommendation = found.get();
        try {
            recommendation.ratePreference(new AccountId(command.accountId()),
                    command.usefulnessRating(), command.favorite());
        } catch (IllegalStateException exception) {
            return Result.failure(CrisisManagementErrors.invalidRecommendationOperation(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return Result.failure(CrisisManagementErrors.invalidRecommendationData(exception.getMessage()));
        }
        return Result.success(recommendationRepository.save(recommendation));
    }

    /**
     * Reacciona a un episodio registrado. Si el episodio no aporta información nueva no cambia
     * nada. Si la aporta, cada recomendación cuyo contenido cambia se reemplaza por una versión
     * nueva y la anterior queda desactualizada, sin borrarse.
     */
    @Override
    public Result<List<Recommendation>, DomainError> handle(RefreshRecommendationsCommand command) {
        var profileId = new ProfileId(command.profileId());
        var current = recommendationRepository.findByProfileId(profileId);
        if (!command.relevant()) {
            return Result.success(current);
        }
        var trigger = command.trigger() == null ? SensoryTrigger.NONE : command.trigger();
        if (current.isEmpty()) {
            return Result.success(generate(profileId, trigger));
        }
        var now = Instant.now(clock);
        for (var recommendation : current) {
            var newContent = RecommendationGenerator.contentFor(recommendation.getTopic(), trigger);
            if (newContent.equals(recommendation.getContent())) {
                continue;
            }
            var replacement = recommendation.replaceWith(newContent, now);
            recommendationRepository.save(recommendation);
            recommendationRepository.save(replacement);
        }
        return Result.success(recommendationRepository.findByProfileId(profileId));
    }

    private List<Recommendation> generate(ProfileId profileId, SensoryTrigger trigger) {
        return RecommendationGenerator.generateFor(profileId, trigger, Instant.now(clock)).stream()
                .map(recommendationRepository::save)
                .toList();
    }

    private SensoryTrigger mostSensitiveTrigger(ProfileId profileId) {
        return externalProfileService.fetchSensoryProfile(profileId)
                .map(SensoryProfileSnapshot::mostSensitiveTrigger)
                .orElse(SensoryTrigger.NONE);
    }
}
