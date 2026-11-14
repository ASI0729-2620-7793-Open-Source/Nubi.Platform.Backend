package com.acme.nubi.platform.shared.domain.events;

/**
 * Publica eventos de dominio para que otros bounded contexts reaccionen a ellos.
 */
public interface DomainEventPublisher {

    void publish(Object event);
}
