package com.acme.nubi.platform.selfregulation.interfaces.rest.resources;

/**
 * @param resourceId   recurso elegido en la galería; puede omitirse
 * @param startedBySos si la sesión la abre el Modo SOS; por defecto es falso
 */
public record StartCalmSessionResource(Long profileId, Long resourceId, Boolean startedBySos) {
}
