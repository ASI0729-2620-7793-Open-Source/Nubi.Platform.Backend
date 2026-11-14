package com.acme.nubi.platform.shared.domain.exceptions;

/**
 * Se lanza cuando un recurso de dominio solicitado no existe. Se traduce a 404 en la capa REST.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super("%s no encontrado: %s".formatted(resource, id));
    }
}
