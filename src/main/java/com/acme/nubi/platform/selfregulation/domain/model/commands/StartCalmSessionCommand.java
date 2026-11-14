package com.acme.nubi.platform.selfregulation.domain.model.commands;

/**
 * @param resourceId recurso elegido en la galería; puede ser {@code null}
 */
public record StartCalmSessionCommand(Long profileId, Long resourceId, boolean startedBySos) {

    public StartCalmSessionCommand {
        if (profileId == null || profileId <= 0) {
            throw new IllegalArgumentException("The profile id must be a positive number.");
        }
        if (resourceId != null && resourceId <= 0) {
            throw new IllegalArgumentException("The calming resource id must be a positive number.");
        }
    }
}
