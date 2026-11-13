package com.acme.nubi.platform.crisismanagement.domain.model.valueobjects;

/**
 * Cómo ve el cuidador al usuario al cerrar el episodio. Determina la intensidad final que se
 * registra cuando el cuidador no indica una intensidad explícita.
 */
public enum ObservedState {
    CALM(EmotionalIntensityLevel.CALM),
    TIRED(EmotionalIntensityLevel.RESTLESS),
    SENSITIVE(EmotionalIntensityLevel.RESTLESS),
    IRRITABLE(EmotionalIntensityLevel.ALTERED);

    private final EmotionalIntensityLevel finalIntensity;

    ObservedState(EmotionalIntensityLevel finalIntensity) {
        this.finalIntensity = finalIntensity;
    }

    public EmotionalIntensityLevel finalIntensity() {
        return finalIntensity;
    }
}
