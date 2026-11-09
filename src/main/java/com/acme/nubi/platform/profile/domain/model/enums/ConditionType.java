package com.acme.nubi.platform.profile.domain.model.enums;

/**
 * Condición declarada por el cuidador (US-04). NUBI no valida ni emite
 * diagnósticos: solo registra lo que la familia informa.
 */
public enum ConditionType {
    ASD,
    ADHD,
    OCD,
    /** Requiere customDescription obligatoria en el Value Object. */
    OTHER
}
