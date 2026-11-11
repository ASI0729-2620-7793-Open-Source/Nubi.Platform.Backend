package com.acme.nubi.platform.selfregulation.interfaces.rest.resources;

import java.time.Instant;

/**
 * @param timer             temporizador de calma, o {@code null} si la sesión no lo usa
 * @param shouldSuggestHelp verdadero si el temporizador terminó y la sesión sigue abierta
 */
public record CalmSessionResource(
        Long id,
        Long profileId,
        Long resourceId,
        String status,
        int intensity,
        boolean lowStimulationActive,
        boolean startedBySos,
        Instant startedAt,
        Instant endedAt,
        CalmTimerResource timer,
        boolean shouldSuggestHelp) {
}
