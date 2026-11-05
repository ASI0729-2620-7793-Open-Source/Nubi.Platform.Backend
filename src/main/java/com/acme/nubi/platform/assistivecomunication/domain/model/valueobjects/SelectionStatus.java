package com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects;

/**
 * Estado de un mensaje enviado al seleccionar un pictograma.
 * PENDING_DELIVERY -> DELIVERED -> UNDERSTOOD | NOT_UNDERSTOOD
 */
public enum SelectionStatus {
    PENDING_DELIVERY,
    DELIVERED,
    UNDERSTOOD,
    NOT_UNDERSTOOD
}
