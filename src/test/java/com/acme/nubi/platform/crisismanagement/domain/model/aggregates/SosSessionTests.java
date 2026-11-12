package com.acme.nubi.platform.crisismanagement.domain.model.aggregates;

import com.acme.nubi.platform.crisismanagement.domain.model.GuideFixtures;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.AccountId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ObservedState;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SosSessionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SosSessionTests {

    private static final Instant START = Instant.parse("2026-11-12T10:00:00Z");

    private final ActionGuide guide = GuideFixtures.threeStepGuide();

    private SosSession start(SensoryTrigger trigger) {
        return SosSession.start(new ProfileId(1L), new AccountId(2L), guide, trigger, null, START);
    }

    @Test
    void startsAlteredAtTheFirstStepOfTheGuideOrderedByTheTrigger() {
        var generic = start(SensoryTrigger.NONE);
        var tactile = start(SensoryTrigger.TACTILE);

        assertThat(generic.getStatus()).isEqualTo(SosSessionStatus.IN_PROGRESS);
        assertThat(generic.getInitialIntensity()).isEqualTo(EmotionalIntensityLevel.ALTERED);
        assertThat(generic.getCurrentStepOrder()).isEqualTo(1);
        assertThat(generic.getTotalSteps()).isEqualTo(3);
        assertThat(tactile.getCurrentStepOrder()).isEqualTo(3);
    }

    @Test
    void treatsAMissingTriggerAsNone() {
        assertThat(start(null).getTrigger()).isEqualTo(SensoryTrigger.NONE);
    }

    @Test
    void advancesToTheNextPendingStepOfTheOrderedGuide() {
        var session = start(SensoryTrigger.TACTILE);
        var ordered = guide.orderedStepsFor(SensoryTrigger.TACTILE);

        session.completeStep(guide.getStep(3), ordered, START.plusSeconds(30));

        assertThat(session.getCurrentStepOrder()).isEqualTo(1);
        assertThat(session.getCompletedSteps()).hasSize(1);
        assertThat(session.hasHandled(30L)).isTrue();
    }

    @Test
    void completingTheSameStepTwiceHasNoEffect() {
        var session = start(SensoryTrigger.NONE);
        var ordered = guide.orderedStepsFor(SensoryTrigger.NONE);

        session.completeStep(guide.getStep(1), ordered, START.plusSeconds(10));
        session.completeStep(guide.getStep(1), ordered, START.plusSeconds(20));

        assertThat(session.getCompletedSteps()).hasSize(1);
    }

    @Test
    void skippingAMandatoryStepNeedsExplicitConfirmation() {
        var session = start(SensoryTrigger.NONE);
        var ordered = guide.orderedStepsFor(SensoryTrigger.NONE);

        assertThatThrownBy(() -> session.skipStep(guide.getStep(1), ordered, false, START.plusSeconds(10)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(session.getCompletedSteps()).isEmpty();

        session.skipStep(guide.getStep(1), ordered, true, START.plusSeconds(10));

        assertThat(session.getCompletedSteps()).singleElement()
                .satisfies(step -> assertThat(step.isSkipped()).isTrue());
    }

    @Test
    void skippingAnOptionalStepNeedsNoConfirmation() {
        var session = start(SensoryTrigger.NONE);

        session.skipStep(guide.getStep(2), guide.orderedStepsFor(SensoryTrigger.NONE), false, START.plusSeconds(10));

        assertThat(session.hasHandled(20L)).isTrue();
    }

    @Test
    void marksTheLastPendingStep() {
        var session = start(SensoryTrigger.NONE);
        var ordered = guide.orderedStepsFor(SensoryTrigger.NONE);

        assertThat(session.isLastStep()).isFalse();
        session.completeStep(guide.getStep(1), ordered, START.plusSeconds(10));
        session.completeStep(guide.getStep(2), ordered, START.plusSeconds(20));

        assertThat(session.isLastStep()).isTrue();
    }

    @Test
    void finishesOnlyAfterEveryStepWasHandled() {
        var session = start(SensoryTrigger.NONE);
        var ordered = guide.orderedStepsFor(SensoryTrigger.NONE);
        session.completeStep(guide.getStep(1), ordered, START.plusSeconds(10));

        assertThatThrownBy(() -> session.finish(ObservedState.CALM, null, START.plusSeconds(60)))
                .isInstanceOf(IllegalStateException.class);

        session.completeStep(guide.getStep(2), ordered, START.plusSeconds(20));
        session.skipStep(guide.getStep(3), ordered, false, START.plusSeconds(30));
        session.finish(ObservedState.CALM, null, START.plusSeconds(120));

        assertThat(session.getStatus()).isEqualTo(SosSessionStatus.FINISHED);
        assertThat(session.getFinalIntensity()).isEqualTo(EmotionalIntensityLevel.CALM);
        assertThat(session.getFinishedAt()).isEqualTo(START.plusSeconds(120));
        assertThat(session.durationInMinutes()).isEqualTo(2);
    }

    @Test
    void derivesTheFinalIntensityFromTheObservedStateUnlessItIsGiven() {
        var derived = start(SensoryTrigger.NONE);
        derived.finishEarly(ObservedState.IRRITABLE, null, START.plusSeconds(90));
        var explicit = start(SensoryTrigger.NONE);
        explicit.finishEarly(ObservedState.TIRED, EmotionalIntensityLevel.CALM, START.plusSeconds(90));

        assertThat(derived.getFinalIntensity()).isEqualTo(EmotionalIntensityLevel.ALTERED);
        assertThat(explicit.getFinalIntensity()).isEqualTo(EmotionalIntensityLevel.CALM);
    }

    @Test
    void finishesEarlyOnlyWhileStepsArePending() {
        var session = start(SensoryTrigger.NONE);

        session.finishEarly(ObservedState.TIRED, null, START.plusSeconds(30));

        assertThat(session.getStatus()).isEqualTo(SosSessionStatus.FINISHED_EARLY);
        assertThat(session.getFinalIntensity()).isEqualTo(EmotionalIntensityLevel.RESTLESS);
        assertThat(session.durationInMinutes()).isEqualTo(1);
    }

    @Test
    void rejectsFinishingEarlyWhenEverythingWasHandled() {
        var session = start(SensoryTrigger.NONE);
        var ordered = guide.orderedStepsFor(SensoryTrigger.NONE);
        session.completeStep(guide.getStep(1), ordered, START.plusSeconds(10));
        session.completeStep(guide.getStep(2), ordered, START.plusSeconds(20));
        session.completeStep(guide.getStep(3), ordered, START.plusSeconds(30));

        assertThatThrownBy(() -> session.finishEarly(null, null, START.plusSeconds(40)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aClosedSessionRejectsFurtherChanges() {
        var session = start(SensoryTrigger.NONE);
        var ordered = guide.orderedStepsFor(SensoryTrigger.NONE);
        session.finishEarly(null, null, START.plusSeconds(30));

        assertThatThrownBy(() -> session.completeStep(guide.getStep(1), ordered, START.plusSeconds(40)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> session.finishEarly(null, null, START.plusSeconds(50)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsAnEndTimeBeforeTheStartTime() {
        var session = start(SensoryTrigger.NONE);

        assertThatThrownBy(() -> session.finishEarly(null, null, START.minusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(session.isOpen()).isTrue();
    }

    @Test
    void anOpenSessionHasNoDuration() {
        assertThat(start(SensoryTrigger.NONE).durationInMinutes()).isZero();
    }
}
