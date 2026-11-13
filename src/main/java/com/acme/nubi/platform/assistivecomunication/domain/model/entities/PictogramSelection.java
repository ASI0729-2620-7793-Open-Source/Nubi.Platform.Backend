package com.acme.nubi.platform.assistivecomunication.domain.model.entities;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.SelectionStatus;
import com.acme.nubi.platform.shared.domain.exceptions.DomainRuleViolationException;
import lombok.Getter;

import java.time.LocalDateTime;

/** Mensaje que el usuario envía al seleccionar un pictograma del tablero. */
@Getter
public class PictogramSelection {
    private Long id;
    private final Long boardId;
    private final Long pictogramId;
    private SelectionStatus status;
    private final LocalDateTime selectedAt;
    private LocalDateTime understoodAt;

    public PictogramSelection(Long id, Long boardId, Long pictogramId, SelectionStatus status,
                              LocalDateTime selectedAt, LocalDateTime understoodAt) {
        if (boardId == null || pictogramId == null || selectedAt == null || status == null) {
            throw new IllegalArgumentException("La selección requiere tablero, pictograma, estado y fecha");
        }
        if (understoodAt != null && understoodAt.isBefore(selectedAt)) {
            throw new IllegalArgumentException("La confirmación no puede ser anterior a la selección");
        }
        this.id = id;
        this.boardId = boardId;
        this.pictogramId = pictogramId;
        this.status = status;
        this.selectedAt = selectedAt;
        this.understoodAt = understoodAt;
    }

    /** Selección nueva: queda pendiente de entrega hasta que el cuidador la reciba. */
    public PictogramSelection(Long boardId, Long pictogramId) {
        this(null, boardId, pictogramId, SelectionStatus.PENDING_DELIVERY, LocalDateTime.now(), null);
    }

    public boolean isPendingDelivery() {
        return status == SelectionStatus.PENDING_DELIVERY;
    }

    /** El mensaje llegó al cuidador. */
    public void markAsDelivered() {
        if (status != SelectionStatus.PENDING_DELIVERY) {
            throw new DomainRuleViolationException(
                    "Solo se puede marcar como entregada una selección pendiente de entrega");
        }
        this.status = SelectionStatus.DELIVERED;
    }

    /** El acompañante confirma que entendió el mensaje. */
    public void confirmUnderstanding() {
        ensureNotClosed();
        this.status = SelectionStatus.UNDERSTOOD;
        this.understoodAt = LocalDateTime.now();
    }

    /** El acompañante no confirmó la comprensión: se generará una solicitud de ayuda. */
    public void markAsNotUnderstood() {
        ensureNotClosed();
        this.status = SelectionStatus.NOT_UNDERSTOOD;
    }

    private void ensureNotClosed() {
        if (status == SelectionStatus.UNDERSTOOD || status == SelectionStatus.NOT_UNDERSTOOD) {
            throw new DomainRuleViolationException("La selección ya fue resuelta (" + status + ")");
        }
    }
}
