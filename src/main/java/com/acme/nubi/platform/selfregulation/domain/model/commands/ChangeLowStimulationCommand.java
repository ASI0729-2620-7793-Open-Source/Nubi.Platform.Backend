package com.acme.nubi.platform.selfregulation.domain.model.commands;

public record ChangeLowStimulationCommand(Long calmSessionId, Boolean active) {

    public ChangeLowStimulationCommand {
        if (calmSessionId == null || calmSessionId <= 0) {
            throw new IllegalArgumentException("The calm session id must be a positive number.");
        }
        if (active == null) {
            throw new IllegalArgumentException("Indicate whether the low stimulation mode is active.");
        }
    }
}
