package com.acme.nubi.platform.crisismanagement.domain.model.entities;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

/**
 * Paso de una guía de actuación. Se muestra de uno en uno y en orden; si es obligatorio, omitirlo
 * exige confirmación explícita del cuidador.
 */
@Getter
public class ContainmentStep {

    private final Long id;
    private final int stepOrder;
    /** Identifica el texto del paso en los idiomas del cliente (por ejemplo SENSORY_ISOLATION). */
    private final String code;
    private final String title;
    private final String description;
    private final String mediaUrl;
    private final boolean mandatory;
    private final SensoryTrigger relatedTrigger;
    private final List<AlternativeTechnique> alternativeTechniques;

    public ContainmentStep(Long id, int stepOrder, String code, String title, String description,
                           String mediaUrl, boolean mandatory, SensoryTrigger relatedTrigger,
                           List<AlternativeTechnique> alternativeTechniques) {
        if (stepOrder <= 0) {
            throw new IllegalArgumentException("The step order must be greater than zero.");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("The step code is required.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("The step title is required.");
        }
        this.id = id;
        this.stepOrder = stepOrder;
        this.code = code;
        this.title = title;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.mandatory = mandatory;
        this.relatedTrigger = relatedTrigger == null ? SensoryTrigger.NONE : relatedTrigger;
        this.alternativeTechniques = alternativeTechniques == null ? List.of() : List.copyOf(alternativeTechniques);
    }

    /** Un paso sin detonante asociado ({@code NONE}) no se relaciona con ninguno. */
    public boolean isRelatedTo(SensoryTrigger trigger) {
        return trigger != null && trigger != SensoryTrigger.NONE && relatedTrigger == trigger;
    }

    public boolean hasAlternativeTechniques() {
        return !alternativeTechniques.isEmpty();
    }

    /** Técnica que se sugiere primero cuando el paso no funciona. */
    public Optional<AlternativeTechnique> findAlternativeTechnique() {
        return alternativeTechniques.stream().findFirst();
    }
}
