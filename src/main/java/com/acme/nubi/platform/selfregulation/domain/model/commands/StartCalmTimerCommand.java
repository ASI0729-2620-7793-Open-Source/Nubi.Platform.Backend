package com.acme.nubi.platform.selfregulation.domain.model.commands;

public record StartCalmTimerCommand(Long calmSessionId, Integer durationMinutes) {

    public StartCalmTimerCommand {
        if (calmSessionId == null || calmSessionId <= 0) {
            throw new IllegalArgumentException("The calm session id must be a positive number.");
        }
        if (durationMinutes == null) {
            throw new IllegalArgumentException("The calm timer duration is required.");
        }
    }
}
