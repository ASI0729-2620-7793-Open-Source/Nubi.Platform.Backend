package com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects;

/**
 * Referencia lógica al perfil del usuario (Bounded Context Perfil y Personalización).
 * No hay clave foránea: la integridad entre contextos se mantiene con eventos y consultas.
 */
public record ProfileId(Long profileId) {
    public ProfileId {
        if (profileId == null || profileId <= 0) {
            throw new IllegalArgumentException("El identificador del perfil debe ser un número positivo");
        }
    }
}
