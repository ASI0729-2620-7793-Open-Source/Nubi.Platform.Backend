package com.acme.nubi.platform.crisismanagement.domain.model.aggregates;

import com.acme.nubi.platform.crisismanagement.domain.model.GuideFixtures;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.ContainmentStep;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensitivityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryProfileSnapshot;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActionGuideTests {

    private final ActionGuide guide = GuideFixtures.threeStepGuide();

    @Test
    void keepsTheOriginalOrderWithoutATrigger() {
        assertThat(codes(guide.orderedStepsFor(SensoryTrigger.NONE)))
                .containsExactly("SENSORY_ISOLATION", "EMOTIONAL_VALIDATION", "BITE_TOOL");
        assertThat(codes(guide.orderedStepsFor((SensoryTrigger) null)))
                .containsExactly("SENSORY_ISOLATION", "EMOTIONAL_VALIDATION", "BITE_TOOL");
    }

    @Test
    void movesTheStepsRelatedToTheTriggerFirst() {
        assertThat(codes(guide.orderedStepsFor(SensoryTrigger.TACTILE)))
                .containsExactly("BITE_TOOL", "SENSORY_ISOLATION", "EMOTIONAL_VALIDATION");
    }

    @Test
    void usesTheMostSensitiveSenseOfTheSensoryProfile() {
        var snapshot = new SensoryProfileSnapshot(new ProfileId(1L),
                SensitivityLevel.LOW, SensitivityLevel.MEDIUM, SensitivityLevel.VERY_HIGH);

        assertThat(codes(guide.orderedStepsFor(snapshot))).first().isEqualTo("BITE_TOOL");
    }

    @Test
    void usesTheGenericGuideWhenTheProfileHasNoSensitivities() {
        var snapshot = new SensoryProfileSnapshot(new ProfileId(1L),
                SensitivityLevel.LOW, SensitivityLevel.LOW, SensitivityLevel.LOW);

        assertThat(snapshot.hasSensitivities()).isFalse();
        assertThat(codes(guide.orderedStepsFor(snapshot))).first().isEqualTo("SENSORY_ISOLATION");
    }

    @Test
    void breaksSensitivityTiesInTheOrderAuditoryVisualTactile() {
        var snapshot = new SensoryProfileSnapshot(new ProfileId(1L),
                SensitivityLevel.HIGH, SensitivityLevel.HIGH, SensitivityLevel.HIGH);

        assertThat(snapshot.mostSensitiveTrigger()).isEqualTo(SensoryTrigger.AUDITORY);
        assertThat(snapshot.highSensitivities())
                .containsExactly(SensoryTrigger.AUDITORY, SensoryTrigger.VISUAL, SensoryTrigger.TACTILE);
    }

    @Test
    void findsAStepById() {
        assertThat(guide.findStepById(20L)).isPresent();
        assertThat(guide.findStepById(999L)).isEmpty();
        assertThat(guide.getStep(3).getCode()).isEqualTo("BITE_TOOL");
    }

    @Test
    void offersTheFirstAlternativeTechniqueOfAStep() {
        assertThat(guide.getStep(1).findAlternativeTechnique())
                .hasValueSatisfying(technique -> assertThat(technique.getCode()).isEqualTo("NOISE_CANCELLING_HEADPHONES"));
        assertThat(guide.getStep(2).findAlternativeTechnique()).isEmpty();
    }

    @Test
    void rejectsAGuideWithoutSteps() {
        assertThatThrownBy(() -> new ActionGuide(null, "Vacía", null, 1, true, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsRepeatedStepOrders() {
        var first = new ContainmentStep(null, 1, "A", "A", null, null, false, SensoryTrigger.NONE, List.of());
        var repeated = new ContainmentStep(null, 1, "B", "B", null, null, false, SensoryTrigger.NONE, List.of());

        assertThatThrownBy(() -> new ActionGuide(null, "Repetida", null, 1, true, List.of(first, repeated)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static List<String> codes(List<ContainmentStep> steps) {
        return steps.stream().map(ContainmentStep::getCode).toList();
    }
}
