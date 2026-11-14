package com.acme.nubi.platform.selfregulation.domain.model.commands;

public record MarkFavoriteResourceCommand(Long profileId, Long resourceId) {

    public MarkFavoriteResourceCommand {
        if (profileId == null || profileId <= 0) {
            throw new IllegalArgumentException("The profile id must be a positive number.");
        }
        if (resourceId == null || resourceId <= 0) {
            throw new IllegalArgumentException("The calming resource id must be a positive number.");
        }
    }
}
