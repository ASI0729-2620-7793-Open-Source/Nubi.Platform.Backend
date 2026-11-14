package com.acme.nubi.platform.visualroutine.interfaces.rest.resources;

import com.acme.nubi.platform.visualroutine.domain.model.aggregates.RoutineStep;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Recursos REST del Bounded Context Rutina Visual. */
public final class RoutineResources {

    private RoutineResources() {
    }

    /** Alta o edición de un paso. {@code scheduledTime} con formato HH:mm, opcional. */
    public record SaveRoutineStepResource(String title, String icon, LocalTime scheduledTime) {
    }

    public record ReorderRoutineResource(List<Long> order) {
    }

    public record RoutineStepResource(
            Long id,
            Long profileId,
            String title,
            String icon,
            LocalTime scheduledTime,
            int position,
            boolean completedToday,
            LocalDate lastCompletedOn) {

        public static RoutineStepResource from(RoutineStep step, LocalDate today) {
            return new RoutineStepResource(step.getId(), step.getProfileId(), step.getTitle(), step.getIcon(),
                    step.getScheduledTime(), step.getPosition(), step.isCompletedOn(today),
                    step.getLastCompletedOn());
        }
    }
}
