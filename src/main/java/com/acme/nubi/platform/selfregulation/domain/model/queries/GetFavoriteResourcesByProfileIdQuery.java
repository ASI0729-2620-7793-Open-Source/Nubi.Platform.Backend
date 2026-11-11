package com.acme.nubi.platform.selfregulation.domain.model.queries;

public record GetFavoriteResourcesByProfileIdQuery(Long profileId) {

    public GetFavoriteResourcesByProfileIdQuery {
        if (profileId == null || profileId <= 0) {
            throw new IllegalArgumentException("The profile id must be a positive number.");
        }
    }
}
