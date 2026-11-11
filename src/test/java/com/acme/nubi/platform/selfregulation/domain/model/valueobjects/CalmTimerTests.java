package com.acme.nubi.platform.selfregulation.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalmTimerTests {

    @Test
    void startsRunningWithTheWholeDuration() {
        var timer = CalmTimer.start(5);

        assertEquals(300, timer.remainingSeconds());
        assertTrue(timer.running());
        assertFalse(timer.isExpired());
    }

    @Test
    void tickNeverLeavesNegativeTimeAndStopsAtZero() {
        var timer = CalmTimer.start(1).tick(45).tick(30);

        assertEquals(0, timer.remainingSeconds());
        assertFalse(timer.running());
        assertTrue(timer.isExpired());
    }

    @Test
    void pausedTimerDoesNotAdvance() {
        var paused = CalmTimer.start(3).pause();

        assertSame(paused, paused.tick(20));
        assertEquals(180, paused.remainingSeconds());
    }

    @Test
    void expiredTimerCannotBeResumed() {
        var expired = CalmTimer.start(1).tick(60);

        assertFalse(expired.resume().running());
    }

    @Test
    void rejectsDurationsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> CalmTimer.start(0));
        assertThrows(IllegalArgumentException.class, () -> CalmTimer.start(CalmTimer.MAX_DURATION_MINUTES + 1));
    }
}
