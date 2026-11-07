package com.acme.nubi.platform.crisismanagement.domain.model.commands;

/**
 * @param confirmed el cuidador confirmó de forma explícita que omite el paso; obligatorio en pasos obligatorios
 */
public record SkipStepCommand(Long sosSessionId, Long stepId, boolean confirmed) {

    public SkipStepCommand {
        if (sosSessionId == null || sosSessionId <= 0) {
            throw new IllegalArgumentException("The SOS session id must be a positive number.");
        }
        if (stepId == null || stepId <= 0) {
            throw new IllegalArgumentException("The step id must be a positive number.");
        }
    }
}
