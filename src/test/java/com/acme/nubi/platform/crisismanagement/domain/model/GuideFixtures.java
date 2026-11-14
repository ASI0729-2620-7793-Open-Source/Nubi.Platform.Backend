package com.acme.nubi.platform.crisismanagement.domain.model;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.ActionGuide;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.AlternativeTechnique;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.ContainmentStep;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;

import java.util.List;

/** Guía de tres pasos para las pruebas de dominio. */
public final class GuideFixtures {

    private GuideFixtures() {
    }

    /**
     * 1 = aislar (auditivo, obligatorio, con técnica alternativa), 2 = validar (sin detonante),
     * 3 = mordedor (táctil).
     */
    public static ActionGuide threeStepGuide() {
        return new ActionGuide(1L, "Guía de prueba", "Guía de tres pasos", 1, true, List.of(
                new ContainmentStep(10L, 1, "SENSORY_ISOLATION", "Aislar", null, null, true,
                        SensoryTrigger.AUDITORY,
                        List.of(new AlternativeTechnique(100L, "NOISE_CANCELLING_HEADPHONES", "Audífonos", null))),
                new ContainmentStep(20L, 2, "EMOTIONAL_VALIDATION", "Validar", null, null, false,
                        SensoryTrigger.NONE, List.of()),
                new ContainmentStep(30L, 3, "BITE_TOOL", "Mordedor", null, null, false,
                        SensoryTrigger.TACTILE, List.of())));
    }
}
