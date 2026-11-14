package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ObservedState;

/**
 * Cambio de estado de la sesión SOS.
 *
 * @param status         FINISHED (cierra la sesión)
 * @param observedState  cómo ve el cuidador al niño al cerrar; opcional
 * @param finalIntensity intensidad final explícita; opcional
 */
public record UpdateSosSessionResource(String status, ObservedState observedState,
                                       EmotionalIntensityLevel finalIntensity) {
}
