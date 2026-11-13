package com.acme.nubi.platform.selfregulation.interfaces.rest.resources;

/**
 * @param elapsedSeconds segundos que el cliente vio pasar desde el último avance del temporizador
 */
public record AdvanceCalmTimerResource(Integer elapsedSeconds) {
}
