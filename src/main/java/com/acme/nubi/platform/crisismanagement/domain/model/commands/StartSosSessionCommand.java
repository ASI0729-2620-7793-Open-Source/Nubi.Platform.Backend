package com.acme.nubi.platform.crisismanagement.domain.model.commands;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;

/**
 * @param trigger          detonante que se identificó; {@code null} equivale a "sin detonante"
 * @param initialIntensity intensidad con la que empieza el episodio; {@code null} usa la predeterminada
 */
public record StartSosSessionCommand(Long profileId, Long caregiverAccountId, SensoryTrigger trigger,
                                     EmotionalIntensityLevel initialIntensity) {

    public StartSosSessionCommand {
        if (profileId == null || profileId <= 0) {
            throw new IllegalArgumentException("The profile id must be a positive number.");
        }
        if (caregiverAccountId == null || caregiverAccountId <= 0) {
            throw new IllegalArgumentException("The caregiver account id must be a positive number.");
        }
    }
}
