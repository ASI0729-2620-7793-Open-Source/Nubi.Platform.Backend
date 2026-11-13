package com.acme.nubi.platform.crisismanagement.domain.model.events;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.shared.domain.model.events.DomainEvent;

import java.time.Instant;

/**
 * Se publica al activar el Modo SOS. Autorregulación lo consume para activar el modo de baja
 * estimulación y Red de Apoyo y Seguimiento para alertar a los contactos de confianza.
 */
public record SosModeActivatedEvent(Long sessionId, Long profileId, Instant activatedAt) implements DomainEvent {

    public static SosModeActivatedEvent from(SosSession session) {
        return new SosModeActivatedEvent(session.getId(), session.getProfileId().value(), session.getStartedAt());
    }
}
