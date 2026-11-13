package com.acme.nubi.platform.profile.domain.model.enums;

/** Rol de un cuidador dentro de un perfil (US-06). */
public enum CaregiverRole {
    /** Quien creó el perfil: no se puede revocar (regla 1). */
    PRIMARY,
    CAREGIVER,
    THERAPIST
}
