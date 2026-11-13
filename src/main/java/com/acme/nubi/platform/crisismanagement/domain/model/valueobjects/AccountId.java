package com.acme.nubi.platform.crisismanagement.domain.model.valueobjects;

/**
 * Referencia lógica a la cuenta del cuidador, que vive en Perfil y Personalización.
 */
public record AccountId(Long value) {

    public AccountId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("The account id must be a positive number.");
        }
    }
}
