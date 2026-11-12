package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * @param caregiverAccountId cuenta del cuidador; el cliente también puede enviarla como caregiverId
 * @param trigger            detonante identificado; si se omite se usa el del perfil sensorial
 * @param initialIntensity   si se omite, la sesión empieza como "Alterado"
 */
public record StartSosSessionResource(
        Long profileId,
        @JsonAlias("caregiverId") Long caregiverAccountId,
        SensoryTrigger trigger,
        EmotionalIntensityLevel initialIntensity) {
}
