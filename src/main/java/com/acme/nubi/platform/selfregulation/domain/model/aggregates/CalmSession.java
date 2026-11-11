package com.acme.nubi.platform.selfregulation.domain.model.aggregates;

import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.CalmSessionStatus;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.CalmTimer;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Sesión de calma de un perfil: el recurso que se está usando, su intensidad, el modo de baja
 * estimulación y el temporizador de calma opcional. Una sesión finalizada ya no admite cambios.
 */
@Getter
public class CalmSession extends AbstractDomainAggregateRoot {

    /** Intensidad de una sesión que todavía no tiene recurso elegido. */
    public static final int DEFAULT_INTENSITY = 5;

    private Long id;
    private final ProfileId profileId;
    private Long resourceId;
    private CalmSessionStatus status;
    private int intensity;
    private boolean lowStimulationActive;
    private final boolean startedBySos;
    private final Instant startedAt;
    private Instant endedAt;
    private CalmTimer timer;

    /**
     * Abre una sesión nueva. Si la abre el Modo SOS, empieza con el modo de baja estimulación activo.
     *
     * @param resource recurso elegido en la galería, o {@code null} si todavía no se eligió ninguno
     */
    public CalmSession(ProfileId profileId, CalmingResource resource, boolean startedBySos, Instant startedAt) {
        this.profileId = Objects.requireNonNull(profileId, "The profile id is required.");
        this.startedAt = Objects.requireNonNull(startedAt, "The start time is required.");
        this.startedBySos = startedBySos;
        this.status = CalmSessionStatus.ACTIVE;
        this.lowStimulationActive = startedBySos;
        this.intensity = DEFAULT_INTENSITY;
        if (resource != null) {
            ensureAvailable(resource);
            this.resourceId = resource.getId();
            this.intensity = resource.defaultIntensity();
        }
    }

    public CalmSession(Long id, ProfileId profileId, Long resourceId, CalmSessionStatus status, int intensity,
                       boolean lowStimulationActive, boolean startedBySos, Instant startedAt, Instant endedAt,
                       CalmTimer timer) {
        this.id = id;
        this.profileId = profileId;
        this.resourceId = resourceId;
        this.status = status;
        this.intensity = intensity;
        this.lowStimulationActive = lowStimulationActive;
        this.startedBySos = startedBySos;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.timer = timer;
    }

    public boolean isOpen() {
        return status != CalmSessionStatus.FINISHED;
    }

    /** Cambia el recurso; la intensidad actual se ajusta al máximo que admite el nuevo recurso. */
    public void selectResource(CalmingResource resource) {
        ensureOpen();
        ensureAvailable(resource);
        this.resourceId = resource.getId();
        this.intensity = resource.clampIntensity(intensity);
    }

    /** Si la intensidad pedida supera el máximo del recurso, se mantiene en el máximo (US-15). */
    public void adjustIntensity(int requestedIntensity, CalmingResource resource) {
        ensureOpen();
        if (resourceId == null || !resourceId.equals(resource.getId())) {
            throw new IllegalStateException("Select a calming resource before adjusting the intensity.");
        }
        this.intensity = resource.clampIntensity(requestedIntensity);
    }

    public void activateLowStimulation() {
        ensureOpen();
        this.lowStimulationActive = true;
    }

    public void deactivateLowStimulation() {
        ensureOpen();
        this.lowStimulationActive = false;
    }

    /** Pausa el estímulo; si hay un temporizador corriendo, también se pausa. */
    public void pause() {
        ensureOpen();
        this.status = CalmSessionStatus.PAUSED;
        if (timer != null) {
            this.timer = timer.pause();
        }
    }

    public void resume() {
        ensureOpen();
        this.status = CalmSessionStatus.ACTIVE;
    }

    public void startTimer(int durationMinutes) {
        ensureOpen();
        this.timer = CalmTimer.start(durationMinutes);
    }

    public void pauseTimer() {
        ensureOpen();
        this.timer = requireTimer().pause();
    }

    public void resumeTimer() {
        ensureOpen();
        this.timer = requireTimer().resume();
    }

    /** Descuenta el tiempo que el cliente vio pasar desde el último avance. */
    public void advanceTimer(int elapsedSeconds) {
        ensureOpen();
        this.timer = requireTimer().tick(elapsedSeconds);
    }

    public void finish(Instant endedAt) {
        ensureOpen();
        this.status = CalmSessionStatus.FINISHED;
        this.endedAt = Objects.requireNonNull(endedAt, "The end time is required.");
        if (timer != null) {
            this.timer = timer.pause();
        }
    }

    /** Política: si el temporizador terminó y la sesión sigue abierta, se sugiere pedir ayuda. */
    public boolean shouldSuggestHelp() {
        return isOpen() && timer != null && timer.isExpired();
    }

    private void ensureOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("The calm session is already finished.");
        }
    }

    private CalmTimer requireTimer() {
        if (timer == null) {
            throw new IllegalStateException("The calm session has no calm timer.");
        }
        return timer;
    }

    private static void ensureAvailable(CalmingResource resource) {
        if (!resource.isActive()) {
            throw new IllegalStateException("The calming resource is not available.");
        }
    }
}
