package com.acme.nubi.platform.crisismanagement.domain.model.aggregates;

import com.acme.nubi.platform.crisismanagement.domain.model.entities.ContainmentStep;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryProfileSnapshot;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Guía de actuación vigente. Es un dato maestro compartido por todos los usuarios: las sesiones
 * SOS la leen, nunca la modifican.
 */
@Getter
public class ActionGuide extends AbstractDomainAggregateRoot {

    private final Long id;
    private final String name;
    private final String description;
    private final int version;
    private final boolean active;
    /** Pasos en el orden original de la guía ({@code stepOrder} ascendente). */
    private final List<ContainmentStep> steps;

    public ActionGuide(Long id, String name, String description, int version, boolean active,
                       List<ContainmentStep> steps) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The action guide name is required.");
        }
        if (version <= 0) {
            throw new IllegalArgumentException("The action guide version must be greater than zero.");
        }
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("The action guide needs at least one step.");
        }
        var orders = new HashSet<Integer>();
        for (var step : steps) {
            if (!orders.add(step.getStepOrder())) {
                throw new IllegalArgumentException(
                        "The step order %d is repeated in the action guide.".formatted(step.getStepOrder()));
            }
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.active = active;
        this.steps = steps.stream()
                .sorted(Comparator.comparingInt(ContainmentStep::getStepOrder))
                .toList();
    }

    public int totalSteps() {
        return steps.size();
    }

    /** Pasos en el orden en que se mostrarán a un usuario con ese perfil sensorial. */
    public List<ContainmentStep> orderedStepsFor(SensoryProfileSnapshot snapshot) {
        return orderedStepsFor(snapshot == null ? SensoryTrigger.NONE : snapshot.mostSensitiveTrigger());
    }

    /**
     * Primero los pasos relacionados con el detonante prioritario y después el resto, respetando
     * {@code stepOrder} dentro de cada grupo. Sin detonante devuelve la guía genérica.
     */
    public List<ContainmentStep> orderedStepsFor(SensoryTrigger priority) {
        if (priority == null || priority == SensoryTrigger.NONE) {
            return steps;
        }
        var ordered = new ArrayList<ContainmentStep>(steps.size());
        steps.stream().filter(step -> step.isRelatedTo(priority)).forEach(ordered::add);
        steps.stream().filter(step -> !step.isRelatedTo(priority)).forEach(ordered::add);
        return List.copyOf(ordered);
    }

    public ContainmentStep getStep(int stepOrder) {
        return steps.stream()
                .filter(step -> step.getStepOrder() == stepOrder)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "The action guide has no step with order %d.".formatted(stepOrder)));
    }

    public Optional<ContainmentStep> findStepById(Long stepId) {
        return steps.stream().filter(step -> step.getId() != null && step.getId().equals(stepId)).findFirst();
    }
}
