package com.acme.nubi.platform.crisismanagement.domain.model.entities;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.AccountId;
import lombok.Getter;

import java.util.Objects;

/**
 * Calificación de utilidad (de 1 a 5) y marca de favorita que un cuidador asigna a una
 * recomendación. Hay una sola por cuidador y recomendación.
 */
@Getter
public class RecommendationPreference {

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    private final Long id;
    private final AccountId accountId;
    private Integer usefulnessRating;
    private boolean favorite;

    public RecommendationPreference(Long id, AccountId accountId, Integer usefulnessRating, boolean favorite) {
        this.id = id;
        this.accountId = Objects.requireNonNull(accountId, "The account id is required.");
        this.usefulnessRating = usefulnessRating == null ? null : validRating(usefulnessRating);
        this.favorite = favorite;
    }

    public void updateRating(Integer rating) {
        this.usefulnessRating = validRating(Objects.requireNonNull(rating, "The usefulness rating is required."));
    }

    public void markFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    private static Integer validRating(Integer rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException(
                    "The usefulness rating must be between %d and %d.".formatted(MIN_RATING, MAX_RATING));
        }
        return rating;
    }
}
