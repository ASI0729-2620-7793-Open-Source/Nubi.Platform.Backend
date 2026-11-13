package com.acme.nubi.platform.selfregulation.domain.model.commands;

public record PauseCalmTimerCommand(Long calmSessionId) {

    public PauseCalmTimerCommand {
        if (calmSessionId == null || calmSessionId <= 0) {
            throw new IllegalArgumentException("The calm session id must be a positive number.");
        }
    }
}
