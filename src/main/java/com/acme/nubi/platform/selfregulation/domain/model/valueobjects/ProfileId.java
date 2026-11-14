package com.acme.nubi.platform.selfregulation.domain.model.valueobjects;

/**
 * Referencia lógica al perfil del usuario autista, que vive en Perfil y Personalización.
 */
public record ProfileId(Long value) {

    public ProfileId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("The profile id must be a positive number.");
        }
    }
}
