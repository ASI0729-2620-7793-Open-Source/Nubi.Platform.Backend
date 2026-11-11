package com.acme.nubi.platform.selfregulation.domain.model.commands;

public record SelectCalmingResourceCommand(Long calmSessionId, Long resourceId) {

    public SelectCalmingResourceCommand {
        if (calmSessionId == null || calmSessionId <= 0) {
            throw new IllegalArgumentException("The calm session id must be a positive number.");
        }
        if (resourceId == null || resourceId <= 0) {
            throw new IllegalArgumentException("The calming resource id must be a positive number.");
        }
    }
}
