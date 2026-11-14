package com.acme.nubi.platform.crisismanagement.domain.model.entities;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Avance de una sesión: un registro por cada paso completado u omitido.
 */
@Getter
public class CompletedStep {

    private final Long id;
    private final Long stepId;
    private final boolean skipped;
    private final Instant completedAt;

    public CompletedStep(Long id, Long stepId, boolean skipped, Instant completedAt) {
        this.id = id;
        this.stepId = Objects.requireNonNull(stepId, "The step id is required.");
        this.skipped = skipped;
        this.completedAt = Objects.requireNonNull(completedAt, "The completion time is required.");
    }
}
