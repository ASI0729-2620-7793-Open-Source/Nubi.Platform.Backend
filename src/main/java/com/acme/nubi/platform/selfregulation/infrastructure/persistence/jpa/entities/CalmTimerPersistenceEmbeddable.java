package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Columnas timer_* de calm_sessions. Una sesión sin temporizador deja la duración y el tiempo
 * restante en null y timer_running en false.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalmTimerPersistenceEmbeddable {

    @Column(name = "timer_duration_minutes")
    private Integer durationMinutes;

    @Column(name = "timer_remaining_seconds")
    private Integer remainingSeconds;

    @Column(name = "timer_running", nullable = false)
    private boolean running;

    public static CalmTimerPersistenceEmbeddable empty() {
        return new CalmTimerPersistenceEmbeddable(null, null, false);
    }
}
