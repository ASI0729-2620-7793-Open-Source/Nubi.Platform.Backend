package com.acme.nubi.platform.crisismanagement.domain.model.commands;

/** Pide una técnica alternativa para un paso que el cuidador no puede aplicar tal como está. */
public record RequestAlternativeCommand(Long sosSessionId, Long stepId) {

    public RequestAlternativeCommand {
        if (sosSessionId == null || sosSessionId <= 0) {
            throw new IllegalArgumentException("The SOS session id must be a positive number.");
        }
        if (stepId == null || stepId <= 0) {
            throw new IllegalArgumentException("The step id must be a positive number.");
        }
    }
}
