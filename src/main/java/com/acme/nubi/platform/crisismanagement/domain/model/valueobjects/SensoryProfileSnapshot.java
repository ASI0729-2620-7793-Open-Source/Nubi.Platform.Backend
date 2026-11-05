package com.acme.nubi.platform.crisismanagement.domain.model.valueobjects;

import java.util.List;
import java.util.Objects;

/**
 * Copia de solo lectura del perfil sensorial que este contexto obtiene de Perfil y
 * Personalización a través del Anti-Corruption Layer.
 */
public record SensoryProfileSnapshot(
        ProfileId profileId,
        SensitivityLevel auditory,
        SensitivityLevel visual,
        SensitivityLevel tactile) {

    /** Orden de desempate cuando dos sentidos tienen la misma sensibilidad. */
    private static final List<SensoryTrigger> SENSES =
            List.of(SensoryTrigger.AUDITORY, SensoryTrigger.VISUAL, SensoryTrigger.TACTILE);

    public SensoryProfileSnapshot {
        Objects.requireNonNull(profileId, "The profile id is required.");
        Objects.requireNonNull(auditory, "The auditory sensitivity is required.");
        Objects.requireNonNull(visual, "The visual sensitivity is required.");
        Objects.requireNonNull(tactile, "The tactile sensitivity is required.");
    }

    /** Sentido con la sensibilidad más alta; {@code NONE} si el perfil no registra ninguna. */
    public SensoryTrigger mostSensitiveTrigger() {
        var highest = SensoryTrigger.NONE;
        var highestRank = 0;
        for (var sense : SENSES) {
            var rank = levelOf(sense).rank();
            if (rank > highestRank) {
                highest = sense;
                highestRank = rank;
            }
        }
        return highest;
    }

    /** Sentidos con sensibilidad alta, para avisar al cuidador qué cuidar durante la guía. */
    public List<SensoryTrigger> highSensitivities() {
        return SENSES.stream()
                .filter(sense -> levelOf(sense).rank() >= SensitivityLevel.HIGH.rank())
                .toList();
    }

    /** Sin sensibilidades registradas se usa la guía genérica y se sugiere completar el perfil. */
    public boolean hasSensitivities() {
        return mostSensitiveTrigger() != SensoryTrigger.NONE;
    }

    private SensitivityLevel levelOf(SensoryTrigger sense) {
        return switch (sense) {
            case AUDITORY -> auditory;
            case VISUAL -> visual;
            case TACTILE -> tactile;
            default -> SensitivityLevel.LOW;
        };
    }
}
