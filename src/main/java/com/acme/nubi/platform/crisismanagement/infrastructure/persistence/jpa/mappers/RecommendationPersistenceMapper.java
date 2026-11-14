package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.mappers;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.RecommendationPreference;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.AccountId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.RecommendationPersistenceEntity;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.RecommendationPreferencePersistenceEntity;

public final class RecommendationPersistenceMapper {

    private RecommendationPersistenceMapper() {
    }

    public static Recommendation toDomain(RecommendationPersistenceEntity entity) {
        return new Recommendation(
                entity.getId(),
                new ProfileId(entity.getProfileId()),
                entity.getTopic(),
                entity.getTitle(),
                entity.getContent(),
                entity.getVersion(),
                entity.isOutdated(),
                entity.getGeneratedAt(),
                entity.getPreferences().stream().map(RecommendationPersistenceMapper::toDomainPreference).toList());
    }

    /** Recomendación nueva: copia todo el agregado. */
    public static RecommendationPersistenceEntity toPersistence(Recommendation recommendation) {
        var entity = new RecommendationPersistenceEntity();
        entity.setProfileId(recommendation.getProfileId().value());
        entity.setTopic(recommendation.getTopic());
        entity.setTitle(recommendation.getTitle());
        entity.setContent(recommendation.getContent());
        entity.setVersion(recommendation.getVersion());
        entity.setGeneratedAt(recommendation.getGeneratedAt());
        apply(entity, recommendation);
        return entity;
    }

    /**
     * Sincroniza una recomendación ya guardada: la marca de desactualizada y las preferencias.
     * Actualiza la fila existente para conservar created_at.
     */
    public static void apply(RecommendationPersistenceEntity entity, Recommendation recommendation) {
        entity.setOutdated(recommendation.isOutdated());
        for (var preference : recommendation.getPreferences()) {
            var saved = entity.getPreferences().stream()
                    .filter(existing -> existing.getAccountId().equals(preference.getAccountId().value()))
                    .findFirst();
            if (saved.isPresent()) {
                saved.get().setUsefulnessRating(preference.getUsefulnessRating());
                saved.get().setFavorite(preference.isFavorite());
            } else {
                entity.getPreferences().add(toPersistencePreference(preference, entity));
            }
        }
    }

    private static RecommendationPreference toDomainPreference(RecommendationPreferencePersistenceEntity entity) {
        return new RecommendationPreference(entity.getId(), new AccountId(entity.getAccountId()),
                entity.getUsefulnessRating(), entity.isFavorite());
    }

    private static RecommendationPreferencePersistenceEntity toPersistencePreference(
            RecommendationPreference preference, RecommendationPersistenceEntity recommendation) {
        var entity = new RecommendationPreferencePersistenceEntity();
        entity.setRecommendation(recommendation);
        entity.setAccountId(preference.getAccountId().value());
        entity.setUsefulnessRating(preference.getUsefulnessRating());
        entity.setFavorite(preference.isFavorite());
        return entity;
    }
}
