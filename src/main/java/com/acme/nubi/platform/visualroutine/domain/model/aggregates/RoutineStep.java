package com.acme.nubi.platform.visualroutine.domain.model.aggregates;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Aggregate root del Bounded Context Rutina Visual: un paso del día del niño (despertar,
 * desayunar, ir al colegio...). Anticipar qué viene después reduce la ansiedad en niños con
 * autismo, por eso cada paso tiene un ícono grande, una hora opcional y se marca como hecho.
 */
public class RoutineStep {

    public static final int MAX_TITLE_LENGTH = 60;

    private final Long id;
    private final Long profileId;
    private String title;
    private String icon;
    private LocalTime scheduledTime;
    private int position;
    private LocalDate lastCompletedOn;

    public RoutineStep(Long id, Long profileId, String title, String icon, LocalTime scheduledTime,
                       int position, LocalDate lastCompletedOn) {
        this.id = id;
        this.profileId = Objects.requireNonNull(profileId, "The profile id is required.");
        this.position = position;
        this.lastCompletedOn = lastCompletedOn;
        rename(title, icon, scheduledTime);
    }

    public static RoutineStep create(Long profileId, String title, String icon, LocalTime scheduledTime,
                                     int position) {
        return new RoutineStep(null, profileId, title, icon, scheduledTime, position, null);
    }

    /** Cambia el texto, el ícono y la hora del paso. */
    public void rename(String newTitle, String newIcon, LocalTime newScheduledTime) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException("El paso necesita un nombre.");
        }
        if (newTitle.trim().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("El nombre del paso admite hasta " + MAX_TITLE_LENGTH + " caracteres.");
        }
        this.title = newTitle.trim();
        this.icon = newIcon == null || newIcon.isBlank() ? "⭐" : newIcon.trim();
        this.scheduledTime = newScheduledTime;
    }

    public void completeOn(LocalDate day) {
        this.lastCompletedOn = Objects.requireNonNull(day);
    }

    public void undoCompletion(LocalDate day) {
        if (isCompletedOn(day)) {
            this.lastCompletedOn = null;
        }
    }

    public boolean isCompletedOn(LocalDate day) {
        return day.equals(lastCompletedOn);
    }

    public void moveTo(int newPosition) {
        this.position = newPosition;
    }

    public Long getId() {
        return id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public int getPosition() {
        return position;
    }

    public LocalDate getLastCompletedOn() {
        return lastCompletedOn;
    }
}
