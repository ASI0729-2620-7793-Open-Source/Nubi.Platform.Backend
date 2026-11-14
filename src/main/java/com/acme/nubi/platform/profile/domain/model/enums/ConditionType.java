package com.acme.nubi.platform.profile.domain.model.enums;

/**
 * Condición declarada por el cuidador (US-04). NUBI acompaña únicamente a niños con
 * Trastorno del Espectro Autista (TEA), por eso la única condición aceptada es {@code ASD}.
 *
 * <p>Los demás valores se conservan solo para poder leer registros antiguos de la base de
 * datos; el Value Object {@code DeclaredDiagnosis} ya no permite declararlos.
 */
public enum ConditionType {
    ASD,
    /** @deprecated Solo para registros antiguos. */
    @Deprecated
    ADHD,
    /** @deprecated Solo para registros antiguos. */
    @Deprecated
    OCD,
    /** @deprecated Solo para registros antiguos. */
    @Deprecated
    OTHER
}
