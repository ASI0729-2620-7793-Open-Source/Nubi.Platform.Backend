package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

/**
 * Cambio de estado de un paso de la guía SOS.
 *
 * @param status    COMPLETED o SKIPPED
 * @param confirmed obligatorio en true para omitir un paso marcado como obligatorio
 */
public record UpdateStepStatusResource(String status, Boolean confirmed) {
}
