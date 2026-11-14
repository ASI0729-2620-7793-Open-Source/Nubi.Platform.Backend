package com.acme.nubi.platform.supportnetwork.interfaces.rest.resources;

/** Nuevo estado de un pedido de ayuda: CONFIRMED o CANCELLED. */
public record UpdateHelpRequestStatusResource(String status) {
}
