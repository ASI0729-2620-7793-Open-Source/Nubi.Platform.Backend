package com.acme.nubi.platform.profile.domain.model.valueobjects;

import com.acme.nubi.platform.profile.domain.model.enums.SensitivityLevel;

/**
 * Value Object inmutable. Perfil sensorial completo de un perfil (US-03).
 * Se reemplaza entero nunca por parches parciales.
 */
public record SensoryProfile(
        SensitivityLevel auditory,
        SensitivityLevel visual,
        SensitivityLevel tactile,
        boolean lowStimulationEnabled,
        boolean prioritizeVisuals,
        boolean confirmAudio) {

    public SensoryProfile {
        if (auditory == null || visual == null || tactile == null) {
            auditory = auditory == null ? SensitivityLevel.MEDIUM : auditory;
            visual = visual == null ? SensitivityLevel.MEDIUM : visual;
            tactile = tactile == null ? SensitivityLevel.MEDIUM : tactile;
        }
    }

    /** Valores por defecto cuando el cuidador no registra sensibilidades (US-03). */
    public static SensoryProfile withDefaultValues() {
        return new SensoryProfile(
                SensitivityLevel.MEDIUM,
                SensitivityLevel.MEDIUM,
                SensitivityLevel.MEDIUM,
                false,
                false,
                false);
    }

    /** Sensibilidad auditiva alta: otros contextos priorizan estímulos visuales. */
    public boolean isAuditorySensitive() {
        return auditory == SensitivityLevel.HIGH || auditory == SensitivityLevel.VERY_HIGH;
    }
}
