package com.acme.nubi.platform.crisismanagement.domain.model.commands;

public record CompleteStepCommand(Long sosSessionId, Long stepId) {

    public CompleteStepCommand {
        if (sosSessionId == null || sosSessionId <= 0) {
            throw new IllegalArgumentException("The SOS session id must be a positive number.");
        }
        if (stepId == null || stepId <= 0) {
            throw new IllegalArgumentException("The step id must be a positive number.");
        }
    }
}
