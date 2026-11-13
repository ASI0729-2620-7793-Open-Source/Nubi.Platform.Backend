package com.acme.nubi.platform.selfregulation.domain.model.valueobjects;

/**
 * Copia de solo lectura del perfil sensorial que este contexto obtiene de Perfil y Personalización.
 */
public record SensoryProfileSnapshot(
        ProfileId profileId,
        SensitivityLevel auditorySensitivity,
        SensitivityLevel visualSensitivity,
        SensitivityLevel tactileSensitivity,
        boolean prioritizeVisuals) {

    public boolean hasHighAuditorySensitivity() {
        return auditorySensitivity == SensitivityLevel.HIGH;
    }

    public boolean hasHighVisualSensitivity() {
        return visualSensitivity == SensitivityLevel.HIGH;
    }
}
