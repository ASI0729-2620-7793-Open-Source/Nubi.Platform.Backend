package com.acme.nubi.platform.selfregulation.domain.model.commands;

public record AdjustIntensityCommand(Long calmSessionId, Integer intensity) {

    public AdjustIntensityCommand {
        if (calmSessionId == null || calmSessionId <= 0) {
            throw new IllegalArgumentException("The calm session id must be a positive number.");
        }
        if (intensity == null) {
            throw new IllegalArgumentException("The intensity is required.");
        }
    }
}
