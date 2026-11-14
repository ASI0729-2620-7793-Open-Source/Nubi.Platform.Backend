package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ObservedState;

/**
 * @param observedState  cómo ve el cuidador al usuario al terminar; opcional
 * @param finalIntensity intensidad final; si se omite se deriva del estado observado
 */
public record FinishSosSessionResource(ObservedState observedState, EmotionalIntensityLevel finalIntensity) {
}
