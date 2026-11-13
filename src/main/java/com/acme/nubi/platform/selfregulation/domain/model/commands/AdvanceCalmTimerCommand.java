package com.acme.nubi.platform.selfregulation.domain.model.commands;

public record AdvanceCalmTimerCommand(Long calmSessionId, Integer elapsedSeconds) {

    public AdvanceCalmTimerCommand {
        if (calmSessionId == null || calmSessionId <= 0) {
            throw new IllegalArgumentException("The calm session id must be a positive number.");
        }
        if (elapsedSeconds == null || elapsedSeconds < 0) {
            throw new IllegalArgumentException("The elapsed seconds must be zero or more.");
        }
    }
}
