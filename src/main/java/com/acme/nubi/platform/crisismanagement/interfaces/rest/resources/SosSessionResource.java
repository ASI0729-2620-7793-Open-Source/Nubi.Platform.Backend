package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

/**
 * @param currentStepOrder paso en el que va el cuidador (stepOrder)
 * @param durationMinutes  minutos del episodio; 0 mientras la sesión sigue abierta
 * @param lastStep         el paso actual es el último que falta atender
 */
public record SosSessionResource(
        Long id,
        Long profileId,
        Long caregiverAccountId,
        Long guideId,
        String status,
        int currentStepOrder,
        int totalSteps,
        String initialIntensity,
        String finalIntensity,
        String trigger,
        String observedState,
        Instant startedAt,
        Instant finishedAt,
        int durationMinutes,
        boolean lastStep,
        List<CompletedStepResource> completedSteps) {
}
