package com.acme.nubi.platform.selfregulation.domain.model.queries;

public record GetLatestCalmSessionByProfileIdQuery(Long profileId) {

    public GetLatestCalmSessionByProfileIdQuery {
        if (profileId == null || profileId <= 0) {
            throw new IllegalArgumentException("The profile id must be a positive number.");
        }
    }
}
