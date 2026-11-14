package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.RecommendationTopic;
import com.acme.nubi.platform.crisismanagement.domain.repositories.RecommendationRepository;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.RecommendationPersistenceEntity;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.mappers.RecommendationPersistenceMapper;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.repositories.RecommendationPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class RecommendationRepositoryAdapter implements RecommendationRepository {

    private final RecommendationPersistenceRepository recommendationPersistenceRepository;

    public RecommendationRepositoryAdapter(RecommendationPersistenceRepository recommendationPersistenceRepository) {
        this.recommendationPersistenceRepository = recommendationPersistenceRepository;
    }

    @Override
    public Recommendation save(Recommendation recommendation) {
        var entity = recommendation.getId() == null
                ? RecommendationPersistenceMapper.toPersistence(recommendation)
                : update(recommendation);
        return RecommendationPersistenceMapper.toDomain(recommendationPersistenceRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Recommendation> findById(Long id) {
        return recommendationPersistenceRepository.findById(id).map(RecommendationPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return recommendationPersistenceRepository.existsById(id);
    }

    @Override
    public List<Recommendation> findByProfileId(ProfileId profileId) {
        return toOrderedDomain(recommendationPersistenceRepository.findAllByProfileIdAndOutdatedFalse(profileId.value()));
    }

    @Override
    public List<Recommendation> findByProfileIdAndTopic(ProfileId profileId, RecommendationTopic topic) {
        return toOrderedDomain(recommendationPersistenceRepository
                .findAllByProfileIdAndTopicAndOutdatedFalse(profileId.value(), topic));
    }

    /** Ordena por tópico según el orden del enum, no alfabético. */
    private List<Recommendation> toOrderedDomain(List<RecommendationPersistenceEntity> entities) {
        return entities.stream()
                .sorted(Comparator.comparing(RecommendationPersistenceEntity::getTopic)
                        .thenComparing(RecommendationPersistenceEntity::getId))
                .map(RecommendationPersistenceMapper::toDomain)
                .toList();
    }

    private RecommendationPersistenceEntity update(Recommendation recommendation) {
        var entity = recommendationPersistenceRepository.findById(recommendation.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "The recommendation %d does not exist.".formatted(recommendation.getId())));
        RecommendationPersistenceMapper.apply(entity, recommendation);
        return entity;
    }
}
