package com.acme.nubi.platform.profile.domain.model.enums;

/** Ciclo de vida de una invitación de cuidador (US-06). */
public enum InvitationStatus {
    /** Invitación enviada, aún sin aceptar: solo tiene correo. */
    PENDING,
    /** Aceptada: ya tiene accountId y fecha de aceptación. */
    ACTIVE,
    /** Acceso retirado por el cuidador principal. */
    REVOKED
}
