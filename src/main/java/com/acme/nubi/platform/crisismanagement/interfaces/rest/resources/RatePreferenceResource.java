package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

/**
 * @param usefulnessRating calificación de utilidad de 1 a 5; si se omite no cambia
 * @param favorite         marca de favorita; si se omite no cambia
 */
public record RatePreferenceResource(Long accountId, Integer usefulnessRating, Boolean favorite) {
}
