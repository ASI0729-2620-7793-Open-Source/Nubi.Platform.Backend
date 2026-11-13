package com.acme.nubi.platform.crisismanagement.domain.model.commands;

/**
 * @param usefulnessRating calificación de utilidad de 1 a 5; {@code null} si no cambia
 * @param favorite         marca de favorita; {@code null} si no cambia
 */
public record RatePreferenceCommand(Long recommendationId, Long accountId, Integer usefulnessRating,
                                    Boolean favorite) {

    public RatePreferenceCommand {
        if (recommendationId == null || recommendationId <= 0) {
            throw new IllegalArgumentException("The recommendation id must be a positive number.");
        }
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException("The account id must be a positive number.");
        }
    }
}
