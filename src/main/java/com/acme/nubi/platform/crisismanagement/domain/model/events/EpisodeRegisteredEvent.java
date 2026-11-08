package com.acme.nubi.platform.crisismanagement.domain.model.events;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.shared.domain.model.events.DomainEvent;

/**
 * Evento que publica Red de Apoyo y Seguimiento cuando registra un episodio. Gestión de Crisis
 * lo consume para actualizar las recomendaciones del perfil.
 *
 * @param relevant si el episodio aporta información nueva sobre lo que le ocurre al usuario
 */
public record EpisodeRegisteredEvent(Long episodeId, Long profileId, SensoryTrigger trigger, boolean relevant)
        implements DomainEvent {
}
