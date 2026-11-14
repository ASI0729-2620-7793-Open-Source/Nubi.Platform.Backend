package com.acme.nubi.platform.crisismanagement.application.internal.commandservices;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.ActionGuide;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.SeedActionGuideCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.AlternativeTechnique;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.ContainmentStep;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.crisismanagement.domain.repositories.ActionGuideRepository;
import com.acme.nubi.platform.crisismanagement.domain.services.ActionGuideCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActionGuideCommandServiceImpl implements ActionGuideCommandService {

    private final ActionGuideRepository actionGuideRepository;

    public ActionGuideCommandServiceImpl(ActionGuideRepository actionGuideRepository) {
        this.actionGuideRepository = actionGuideRepository;
    }

    /**
     * Registra la guía de contención inicial solo si no hay ninguna vigente, así reiniciar la
     * aplicación no duplica el catálogo. Los textos son orientaciones de apoyo cotidiano: no
     * reemplazan la atención de un profesional.
     */
    @Override
    public void handle(SeedActionGuideCommand command) {
        if (actionGuideRepository.findActiveGuide().isPresent()) {
            return;
        }
        var steps = List.of(
                new ContainmentStep(null, 1, "SENSORY_ISOLATION",
                        "Reducir los estímulos",
                        "Baja el ruido y la luz del lugar y aleja al usuario de lo que lo está alterando.",
                        null, true, SensoryTrigger.AUDITORY,
                        List.of(
                                new AlternativeTechnique(null, "NOISE_CANCELLING_HEADPHONES",
                                        "Audífonos con cancelación de ruido",
                                        "Ofrece unos audífonos para aislar los sonidos del entorno."),
                                new AlternativeTechnique(null, "DIM_THE_LIGHTS",
                                        "Bajar las luces",
                                        "Apaga o reduce las luces y cierra las cortinas para disminuir lo visual."))),
                new ContainmentStep(null, 2, "EMOTIONAL_VALIDATION",
                        "Validar lo que siente",
                        "Habla con voz baja y frases cortas. Reconoce su malestar sin discutir ni corregir.",
                        null, false, SensoryTrigger.NONE, List.of()),
                new ContainmentStep(null, 3, "SAFE_SPACE",
                        "Llevarlo a un lugar seguro",
                        "Acompáñalo a un espacio tranquilo y conocido, con pocos estímulos visuales.",
                        null, false, SensoryTrigger.VISUAL, List.of()),
                new ContainmentStep(null, 4, "BITE_TOOL",
                        "Ofrecer un objeto sensorial",
                        "Ofrece un mordedor u objeto seguro para que canalice la tensión.",
                        null, false, SensoryTrigger.TACTILE,
                        List.of(new AlternativeTechnique(null, "CHEWABLE_SNACK",
                                "Snack para masticar",
                                "Si no tiene mordedor, ofrece algo seguro para masticar."))),
                new ContainmentStep(null, 5, "CALM_BREATHING",
                        "Respirar con calma",
                        "Respira despacio a su lado para que pueda seguir tu ritmo.",
                        null, false, SensoryTrigger.NONE, List.of()),
                new ContainmentStep(null, 6, "FINAL_CHECK",
                        "Revisión final",
                        "Observa cómo está ahora y confirma que ya no corre peligro antes de retomar la actividad.",
                        null, false, SensoryTrigger.NONE, List.of()));

        actionGuideRepository.save(new ActionGuide(null, "Guía de contención ante crisis sensorial",
                "Pasos para acompañar a una persona durante una crisis sensorial.", 1, true, steps));
    }
}
