package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.RecommendationTopic;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.RecommendationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationPersistenceRepository extends JpaRepository<RecommendationPersistenceEntity, Long> {
    List<RecommendationPersistenceEntity> findAllByProfileIdAndOutdatedFalse(Long profileId);

    List<RecommendationPersistenceEntity> findAllByProfileIdAndTopicAndOutdatedFalse(
            Long profileId, RecommendationTopic topic);
}
