package com.acme.nubi.platform.selfregulation.domain.model.aggregates;

import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.CalmSessionStatus;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ResourceType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalmSessionTests {

    private static final Instant NOW = Instant.parse("2026-11-11T15:00:00Z");
    private static final ProfileId PROFILE_ID = new ProfileId(1L);

    private static CalmingResource resource(long id, int maxIntensity) {
        return new CalmingResource(id, "RESOURCE_" + id, "Resource " + id, ResourceType.VISUAL,
                "stimuli/resource.jpg", maxIntensity, true, true);
    }

    @Test
    void startsWithTheDefaultIntensityOfTheResource() {
        var session = new CalmSession(PROFILE_ID, resource(1L, 10), false, NOW);

        assertEquals(CalmSessionStatus.ACTIVE, session.getStatus());
        assertEquals(1L, session.getResourceId());
        assertEquals(6, session.getIntensity());
        assertFalse(session.isLowStimulationActive());
    }

    @Test
    void sessionStartedBySosStartsInLowStimulationMode() {
        var session = new CalmSession(PROFILE_ID, null, true, NOW);

        assertTrue(session.isLowStimulationActive());
        assertNull(session.getResourceId());
    }

    @Test
    void intensityAboveTheMaximumStaysAtTheMaximum() {
        var resource = resource(1L, 10);
        var session = new CalmSession(PROFILE_ID, resource, false, NOW);

        session.adjustIntensity(25, resource);

        assertEquals(10, session.getIntensity());
    }

    @Test
    void selectingAResourceWithALowerMaximumLowersTheIntensity() {
        var session = new CalmSession(PROFILE_ID, resource(1L, 10), false, NOW);
        session.adjustIntensity(9, resource(1L, 10));

        session.selectResource(resource(2L, 4));

        assertEquals(2L, session.getResourceId());
        assertEquals(4, session.getIntensity());
    }

    @Test
    void cannotAdjustTheIntensityWithoutAResource() {
        var session = new CalmSession(PROFILE_ID, null, false, NOW);

        assertThrows(IllegalStateException.class, () -> session.adjustIntensity(5, resource(1L, 10)));
    }

    @Test
    void suggestsHelpWhenTheTimerEndsAndTheSessionIsStillOpen() {
        var session = new CalmSession(PROFILE_ID, resource(1L, 10), false, NOW);
        session.startTimer(1);
        session.advanceTimer(60);

        assertTrue(session.shouldSuggestHelp());

        session.finish(NOW.plusSeconds(90));

        assertFalse(session.shouldSuggestHelp());
        assertEquals(CalmSessionStatus.FINISHED, session.getStatus());
    }

    @Test
    void pausingTheSessionAlsoPausesTheTimer() {
        var session = new CalmSession(PROFILE_ID, resource(1L, 10), false, NOW);
        session.startTimer(5);

        session.pause();

        assertEquals(CalmSessionStatus.PAUSED, session.getStatus());
        assertFalse(session.getTimer().running());
    }

    @Test
    void finishedSessionRejectsChanges() {
        var session = new CalmSession(PROFILE_ID, resource(1L, 10), false, NOW);
        session.finish(NOW.plusSeconds(60));

        assertThrows(IllegalStateException.class, session::activateLowStimulation);
        assertThrows(IllegalStateException.class, () -> session.startTimer(3));
        assertThrows(IllegalStateException.class, () -> session.finish(NOW.plusSeconds(120)));
    }

    @Test
    void inactiveResourceCannotBeSelected() {
        var inactive = new CalmingResource(3L, "OLD", "Old resource", ResourceType.AUDITORY, null, 10, false, false);

        assertThrows(IllegalStateException.class, () -> new CalmSession(PROFILE_ID, inactive, false, NOW));
    }
}
