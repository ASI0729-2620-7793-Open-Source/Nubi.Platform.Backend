package com.acme.nubi.platform.selfregulation.domain.model.queries;

public record GetCalmSessionsByProfileIdQuery(Long profileId) {

    public GetCalmSessionsByProfileIdQuery {
        if (profileId == null || profileId <= 0) {
            throw new IllegalArgumentException("The profile id must be a positive number.");
        }
    }
}
