package com.acme.nubi.platform.selfregulation.interfaces.rest.resources;

/** Nuevo estado de la sesión de calma: PAUSED, ACTIVE o FINISHED. */
public record UpdateCalmSessionStatusResource(String status) {
}
