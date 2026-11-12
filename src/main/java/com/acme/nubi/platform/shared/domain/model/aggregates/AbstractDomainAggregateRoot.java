package com.acme.nubi.platform.shared.domain.model.aggregates;

import com.acme.nubi.platform.shared.domain.model.events.DomainEvent;

import java.util.ArrayList;
import java.util.List;

public abstract  class AbstractDomainAggregateRoot {
    private final List<DomainEvent> pendingEvents=new ArrayList<>();
    protected final void registerEvent(DomainEvent domainEvent) {
        pendingEvents.add(domainEvent);
    }
    protected final void unregisterEvent(DomainEvent domainEvent) {
        pendingEvents.remove(domainEvent);
    }
    public final List<DomainEvent> pullDomainEvents() {
        var copy = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return copy;
    }


}
