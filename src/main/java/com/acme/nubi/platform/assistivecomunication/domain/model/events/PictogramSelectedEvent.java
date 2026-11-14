package com.acme.nubi.platform.assistivecomunication.domain.model.events;

import java.time.LocalDateTime;

/**
 * El usuario seleccionó un pictograma. Red de Apoyo y Seguimiento lo usa para avisar al cuidador
 * (por ejemplo "Diana necesita: Tengo sed").
 */
public record PictogramSelectedEvent(Long selectionId, Long profileId, String phrase, LocalDateTime occurredAt) {
}
