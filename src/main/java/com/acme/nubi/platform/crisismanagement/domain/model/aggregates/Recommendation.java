package com.acme.nubi.platform.crisismanagement.domain.model.aggregates;

import com.acme.nubi.platform.crisismanagement.domain.model.entities.RecommendationPreference;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.AccountId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.RecommendationTopic;
import com.acme.nubi.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Recomendación personalizada de un perfil, clasificada por tópico. Se versiona: cuando el
 * contenido cambia se genera una versión nueva y la anterior queda marcada como desactualizada,
 * sin borrarse.
 */
@Getter
public class Recommendation extends AbstractDomainAggregateRoot {

    private final Long id;
    private final ProfileId profileId;
    private final RecommendationTopic topic;
    private final String title;
    private final String content;
    private final int version;
    private boolean outdated;
    private final Instant generatedAt;
    private final List<RecommendationPreference> preferences;

    /** Primera versión de una recomendación. */
    public Recommendation(ProfileId profileId, RecommendationTopic topic, String title, String content,
                          Instant generatedAt) {
        this(null, profileId, topic, title, content, 1, false, generatedAt, List.of());
    }

    public Recommendation(Long id, ProfileId profileId, RecommendationTopic topic, String title, String content,
                          int version, boolean outdated, Instant generatedAt,
                          List<RecommendationPreference> preferences) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("The recommendation title is required.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("The recommendation content is required.");
        }
        if (version <= 0) {
            throw new IllegalArgumentException("The recommendation version must be greater than zero.");
        }
        this.id = id;
        this.profileId = Objects.requireNonNull(profileId, "The profile id is required.");
        this.topic = Objects.requireNonNull(topic, "The topic is required.");
        this.title = title;
        this.content = content;
        this.version = version;
        this.outdated = outdated;
        this.generatedAt = Objects.requireNonNull(generatedAt, "The generation time is required.");
        this.preferences = new ArrayList<>(preferences == null ? List.of() : preferences);
    }

    public List<RecommendationPreference> getPreferences() {
        return Collections.unmodifiableList(preferences);
    }

    public void markAsOutdated() {
        this.outdated = true;
    }

    /**
     * Marca esta versión como desactualizada y devuelve la versión siguiente con el contenido nuevo.
     * Las calificaciones no se trasladan: cada versión se califica por separado.
     */
    public Recommendation replaceWith(String newContent, Instant generatedAt) {
        markAsOutdated();
        return new Recommendation(null, profileId, topic, title, newContent, version + 1, false, generatedAt, List.of());
    }

    /**
     * Registra la calificación de utilidad y/o la marca de favorita de un cuidador. Volver a calificar
     * actualiza la preferencia existente en lugar de crear otra. Solo cambia lo que se indica.
     */
    public RecommendationPreference ratePreference(AccountId accountId, Integer usefulnessRating, Boolean favorite) {
        if (outdated) {
            throw new IllegalStateException("The recommendation is outdated; rate its current version instead.");
        }
        if (usefulnessRating == null && favorite == null) {
            throw new IllegalArgumentException("Provide a usefulness rating or a favorite mark.");
        }
        var preference = preferences.stream()
                .filter(existing -> existing.getAccountId().equals(accountId))
                .findFirst()
                .orElseGet(() -> {
                    var created = new RecommendationPreference(null, accountId, null, false);
                    preferences.add(created);
                    return created;
                });
        if (usefulnessRating != null) {
            preference.updateRating(usefulnessRating);
        }
        if (favorite != null) {
            preference.markFavorite(favorite);
        }
        return preference;
    }
}
