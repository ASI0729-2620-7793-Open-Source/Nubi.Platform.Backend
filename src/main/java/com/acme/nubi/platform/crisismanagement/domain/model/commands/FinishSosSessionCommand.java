package com.acme.nubi.platform.crisismanagement.domain.model.commands;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ObservedState;

/**
 * @param observedState  cómo ve el cuidador al usuario al terminar; puede ser {@code null}
 * @param finalIntensity intensidad final; si es {@code null} se deriva del estado observado
 */
public record FinishSosSessionCommand(Long sosSessionId, ObservedState observedState,
                                      EmotionalIntensityLevel finalIntensity) {

    public FinishSosSessionCommand {
        if (sosSessionId == null || sosSessionId <= 0) {
            throw new IllegalArgumentException("The SOS session id must be a positive number.");
        }
    }
}
