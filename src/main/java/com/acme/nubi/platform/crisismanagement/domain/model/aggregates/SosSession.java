package com.acme.nubi.platform.crisismanagement.domain.model.aggregates;

import com.acme.nubi.platform.crisismanagement.domain.model.entities.CompletedStep;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.ContainmentStep;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.AccountId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ObservedState;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SosSessionStatus;
import com.acme.nubi.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Sesión guiada que acompaña al cuidador durante una crisis: la guía que sigue, el paso en el que
 * va y los pasos completados u omitidos. Una sesión cerrada ya no admite cambios.
 */
@Getter
public class SosSession extends AbstractDomainAggregateRoot {

    /** Toda sesión SOS empieza como "Alterado" y se compara con la intensidad que se observa al terminar. */
    public static final EmotionalIntensityLevel DEFAULT_INITIAL_INTENSITY = EmotionalIntensityLevel.ALTERED;

    private final Long id;
    private final ProfileId profileId;
    private final AccountId caregiverAccountId;
    private final Long guideId;
    private SosSessionStatus status;
    /** {@code stepOrder} del paso en el que va el cuidador; permite retomar la guía si la abandona. */
    private int currentStepOrder;
    private final int totalSteps;
    private final EmotionalIntensityLevel initialIntensity;
    private EmotionalIntensityLevel finalIntensity;
    private final SensoryTrigger trigger;
    private ObservedState observedState;
    private final Instant startedAt;
    private Instant finishedAt;
    private final List<CompletedStep> completedSteps;

    /**
     * Abre una sesión con la guía vigente. La guía se personaliza según el detonante: la sesión
     * empieza en el primer paso de la guía ordenada para ese detonante.
     *
     * @param initialIntensity intensidad con la que empieza el episodio; {@code null} usa {@link #DEFAULT_INITIAL_INTENSITY}
     */
    public static SosSession start(ProfileId profileId, AccountId caregiverAccountId, ActionGuide guide,
                                   SensoryTrigger trigger, EmotionalIntensityLevel initialIntensity,
                                   Instant startedAt) {
        Objects.requireNonNull(guide, "The action guide is required.");
        if (!guide.isActive()) {
            throw new IllegalStateException("The action guide is not active.");
        }
        var resolvedTrigger = trigger == null ? SensoryTrigger.NONE : trigger;
        var firstStep = guide.orderedStepsFor(resolvedTrigger).getFirst();
        return new SosSession(null, profileId, caregiverAccountId, guide.getId(),
                SosSessionStatus.IN_PROGRESS, firstStep.getStepOrder(), guide.totalSteps(),
                initialIntensity == null ? DEFAULT_INITIAL_INTENSITY : initialIntensity,
                null, resolvedTrigger, null, startedAt, null, List.of());
    }

    public SosSession(Long id, ProfileId profileId, AccountId caregiverAccountId, Long guideId,
                      SosSessionStatus status, int currentStepOrder, int totalSteps,
                      EmotionalIntensityLevel initialIntensity, EmotionalIntensityLevel finalIntensity,
                      SensoryTrigger trigger, ObservedState observedState, Instant startedAt,
                      Instant finishedAt, List<CompletedStep> completedSteps) {
        this.id = id;
        this.profileId = Objects.requireNonNull(profileId, "The profile id is required.");
        this.caregiverAccountId = Objects.requireNonNull(caregiverAccountId, "The caregiver account id is required.");
        this.guideId = Objects.requireNonNull(guideId, "The action guide id is required.");
        this.status = Objects.requireNonNull(status, "The status is required.");
        this.currentStepOrder = currentStepOrder;
        this.totalSteps = totalSteps;
        this.initialIntensity = Objects.requireNonNull(initialIntensity, "The initial intensity is required.");
        this.finalIntensity = finalIntensity;
        this.trigger = Objects.requireNonNull(trigger, "The trigger is required.");
        this.observedState = observedState;
        this.startedAt = Objects.requireNonNull(startedAt, "The start time is required.");
        this.finishedAt = finishedAt;
        this.completedSteps = new ArrayList<>(completedSteps == null ? List.of() : completedSteps);
    }

    public List<CompletedStep> getCompletedSteps() {
        return Collections.unmodifiableList(completedSteps);
    }

    public boolean isOpen() {
        return status.isOpen();
    }

    /** El paso ya se completó o se omitió. */
    public boolean hasHandled(Long stepId) {
        return completedSteps.stream().anyMatch(completed -> completed.getStepId().equals(stepId));
    }

    public boolean allStepsHandled() {
        return completedSteps.size() >= totalSteps;
    }

    /** El paso en el que va el cuidador es el último que le falta atender. */
    public boolean isLastStep() {
        return isOpen() && completedSteps.size() == totalSteps - 1;
    }

    /**
     * Registra el paso como completado y avanza al siguiente que todavía no se atendió. Registrar
     * dos veces el mismo paso no tiene efecto.
     *
     * @param orderedSteps pasos de la guía en el orden personalizado de esta sesión
     */
    public void completeStep(ContainmentStep step, List<ContainmentStep> orderedSteps, Instant completedAt) {
        ensureOpen();
        handle(step, orderedSteps, false, completedAt);
    }

    /** Omitir un paso obligatorio exige que el cuidador lo haya confirmado de forma explícita. */
    public void skipStep(ContainmentStep step, List<ContainmentStep> orderedSteps, boolean confirmed,
                         Instant completedAt) {
        ensureOpen();
        if (step.isMandatory() && !confirmed) {
            throw new IllegalArgumentException(
                    "The step %d is mandatory: skipping it requires explicit confirmation.".formatted(step.getId()));
        }
        handle(step, orderedSteps, true, completedAt);
    }

    /**
     * Cierra la sesión cuando ya se atendieron todos los pasos. La intensidad final es la indicada
     * o, si no se indica, la que corresponde al estado observado.
     */
    public void finish(ObservedState observedState, EmotionalIntensityLevel finalIntensity, Instant finishedAt) {
        ensureOpen();
        if (!allStepsHandled()) {
            throw new IllegalStateException("The guide still has pending steps; finish the session early instead.");
        }
        close(SosSessionStatus.FINISHED, observedState, finalIntensity, finishedAt);
    }

    /** Cierra la sesión antes de atender todos los pasos de la guía. */
    public void finishEarly(ObservedState observedState, EmotionalIntensityLevel finalIntensity, Instant finishedAt) {
        ensureOpen();
        if (allStepsHandled()) {
            throw new IllegalStateException("All the steps were handled; finish the session normally instead.");
        }
        close(SosSessionStatus.FINISHED_EARLY, observedState, finalIntensity, finishedAt);
    }

    /** Minutos del episodio, al menos 1; es 0 mientras la sesión sigue abierta. */
    public int durationInMinutes() {
        if (finishedAt == null) {
            return 0;
        }
        var minutes = Math.round(Duration.between(startedAt, finishedAt).toMillis() / 60000.0);
        return (int) Math.max(1, minutes);
    }

    private void handle(ContainmentStep step, List<ContainmentStep> orderedSteps, boolean skipped,
                        Instant completedAt) {
        if (hasHandled(step.getId())) {
            return;
        }
        completedSteps.add(new CompletedStep(null, step.getId(), skipped, completedAt));
        orderedSteps.stream()
                .filter(candidate -> !hasHandled(candidate.getId()))
                .findFirst()
                .ifPresent(next -> this.currentStepOrder = next.getStepOrder());
    }

    private void close(SosSessionStatus closedStatus, ObservedState observedState,
                       EmotionalIntensityLevel finalIntensity, Instant finishedAt) {
        Objects.requireNonNull(finishedAt, "The end time is required.");
        if (finishedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("The end time cannot be before the start time.");
        }
        this.status = closedStatus;
        this.observedState = observedState;
        this.finalIntensity = finalIntensity != null
                ? finalIntensity
                : observedState == null ? null : observedState.finalIntensity();
        this.finishedAt = finishedAt;
    }

    private void ensureOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("The SOS session is already finished.");
        }
    }
}
