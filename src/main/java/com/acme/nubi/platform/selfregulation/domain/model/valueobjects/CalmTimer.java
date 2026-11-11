package com.acme.nubi.platform.selfregulation.domain.model.valueobjects;

/**
 * Temporizador de calma. Es inmutable: cada operación devuelve un temporizador nuevo.
 * El tiempo restante nunca es negativo y un temporizador sin tiempo restante no corre.
 */
public record CalmTimer(int durationMinutes, int remainingSeconds, boolean running) {

    public static final int MAX_DURATION_MINUTES = 60;

    public CalmTimer {
        if (durationMinutes < 1 || durationMinutes > MAX_DURATION_MINUTES) {
            throw new IllegalArgumentException(
                    "The calm timer duration must be between 1 and %d minutes.".formatted(MAX_DURATION_MINUTES));
        }
        if (remainingSeconds < 0 || remainingSeconds > durationMinutes * 60) {
            throw new IllegalArgumentException("The remaining seconds are out of the timer duration.");
        }
        running = running && remainingSeconds > 0;
    }

    public static CalmTimer start(int durationMinutes) {
        return new CalmTimer(durationMinutes, durationMinutes * 60, true);
    }

    public CalmTimer pause() {
        return new CalmTimer(durationMinutes, remainingSeconds, false);
    }

    public CalmTimer resume() {
        return isExpired() ? this : new CalmTimer(durationMinutes, remainingSeconds, true);
    }

    public CalmTimer tick(int elapsedSeconds) {
        if (elapsedSeconds < 0) {
            throw new IllegalArgumentException("The elapsed seconds cannot be negative.");
        }
        if (!running) {
            return this;
        }
        var remaining = Math.max(0, remainingSeconds - elapsedSeconds);
        return new CalmTimer(durationMinutes, remaining, remaining > 0);
    }

    public boolean isExpired() {
        return remainingSeconds == 0;
    }
}
