package com.acme.nubi.platform.crisismanagement.domain.model.valueobjects;

/**
 * Detonante que dispara o agrava la crisis. {@code NONE} indica que el perfil no tiene una
 * sensibilidad marcada o que el paso de la guía no se relaciona con ningún detonante.
 */
public enum SensoryTrigger {
    AUDITORY,
    VISUAL,
    TACTILE,
    ROUTINE_CHANGE,
    CROWD,
    OTHER,
    NONE
}
