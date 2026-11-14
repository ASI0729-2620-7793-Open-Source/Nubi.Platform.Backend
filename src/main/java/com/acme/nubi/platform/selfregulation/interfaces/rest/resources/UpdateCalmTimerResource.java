package com.acme.nubi.platform.selfregulation.interfaces.rest.resources;

/** running = false pausa el temporizador de calma; running = true lo reanuda. */
public record UpdateCalmTimerResource(Boolean running) {
}
