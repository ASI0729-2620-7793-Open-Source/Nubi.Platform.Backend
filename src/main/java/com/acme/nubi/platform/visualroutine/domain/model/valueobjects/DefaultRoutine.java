package com.acme.nubi.platform.visualroutine.domain.model.valueobjects;

import java.time.LocalTime;
import java.util.List;

/**
 * Rutina sugerida con la que arranca todo niño. El cuidador la ajusta después a su día real.
 */
public final class DefaultRoutine {

    public record Template(String title, String icon, LocalTime time) {
    }

    public static final List<Template> STEPS = List.of(
            new Template("Despertar", "☀️", LocalTime.of(7, 0)),
            new Template("Lavarme la cara", "🧼", LocalTime.of(7, 10)),
            new Template("Desayunar", "🥣", LocalTime.of(7, 30)),
            new Template("Cepillarme los dientes", "🪥", LocalTime.of(7, 50)),
            new Template("Ir al colegio", "🎒", LocalTime.of(8, 0)),
            new Template("Almorzar", "🍽️", LocalTime.of(13, 0)),
            new Template("Jugar", "🧩", LocalTime.of(16, 0)),
            new Template("Rincón de calma", "🫧", LocalTime.of(18, 0)),
            new Template("Bañarme", "🛁", LocalTime.of(19, 30)),
            new Template("Dormir", "🌙", LocalTime.of(20, 30)));

    private DefaultRoutine() {
    }
}
