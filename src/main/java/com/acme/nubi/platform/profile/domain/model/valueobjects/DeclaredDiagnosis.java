package com.acme.nubi.platform.profile.domain.model.valueobjects;

import com.acme.nubi.platform.profile.domain.errors.DomainRuleViolation;
import com.acme.nubi.platform.profile.domain.model.enums.ConditionType;
import java.time.LocalDate;

/**
 * Value Object inmutable. Diagnóstico declarado por el cuidador (US-04).
 * Se declara, no se valida: NUBI nunca pide verificación ni documentos.
 *
 * <p>NUBI está pensada solo para niños con autismo (TEA): la condición siempre es
 * {@link ConditionType#ASD}. El cuidador puede completar quién lo diagnosticó, la fecha y
 * notas del profesional (por ejemplo el nivel de apoyo).
 */
public record DeclaredDiagnosis(
        ConditionType condition,
        String customDescription,
        String diagnosedBy,
        LocalDate diagnosisDate,
        String professionalNotes) {

    public DeclaredDiagnosis {
        if (condition == null) {
            condition = ConditionType.ASD;
        }
        if (condition != ConditionType.ASD) {
            throw new DomainRuleViolation(
                    "condition.autism.only",
                    "Nubi acompaña únicamente a niños con autismo (TEA).",
                    "condition");
        }
        if (diagnosisDate != null && diagnosisDate.isAfter(LocalDate.now())) {
            throw new DomainRuleViolation(
                    "diagnosis.date.future",
                    "La fecha del diagnóstico no puede estar en el futuro.",
                    "diagnosisDate");
        }
    }

    /** Diagnóstico con el que nace todo perfil: autismo, sin datos del profesional. */
    public static DeclaredDiagnosis autism() {
        return new DeclaredDiagnosis(ConditionType.ASD, null, null, null, null);
    }

    public String displayCondition() {
        return condition.name();
    }
}
