package com.acme.nubi.platform.profile.domain.events;

import com.acme.nubi.platform.shared.domain.model.events.DomainEvent;
import java.time.Instant;

/**
 * Publicado cuando el cuidador vincula un contacto de confianza.
 * Red de Apoyo y Seguimiento lo consume para incorporarlo a la red de
 * alertas del usuario (Diagrama de flujo: "Avisar a Red de Apoyo").
 */
public record TrustedContactLinkedEvent(
        Long profileId, Long contactId, String fullName, Instant occurredAt)
        implements DomainEvent {
}
