package com.acme.nubi.platform.profile.domain.model.valueobjects;

import com.acme.nubi.platform.profile.domain.errors.DomainRuleViolation;
import com.acme.nubi.platform.profile.domain.model.enums.ConditionType;
import java.time.LocalDate;

/**
 * Value Object inmutable. Diagnóstico declarado por el cuidador (US-04).
 * Se declara, no se valida: NUBI nunca pide verificación ni documentos.
 * La condición OTHER exige una descripción personalizada.
 */
public record DeclaredDiagnosis(
        ConditionType condition,
        String customDescription,
        String diagnosedBy,
        LocalDate diagnosisDate,
        String professionalNotes) {

    public DeclaredDiagnosis {
        if (condition == null) {
            throw new DomainRuleViolation(
                    "condition.required", "La condición es obligatoria.", "condition");
        }
        if (condition == ConditionType.OTHER
                && (customDescription == null || customDescription.isBlank())) {
            throw new DomainRuleViolation(
                    "condition.other.description.required",
                    "La condición \"Otra\" requiere una descripción.",
                    "customDescription");
        }
    }

    /** Etiqueta a mostrar cuando la condición es OTHER (US-04, Scenario 2). */
    public String displayCondition() {
        return condition == ConditionType.OTHER ? customDescription : condition.name();
    }
}
