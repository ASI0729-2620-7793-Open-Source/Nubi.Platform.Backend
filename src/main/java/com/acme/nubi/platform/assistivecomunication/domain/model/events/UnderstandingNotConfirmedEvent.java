package com.acme.nubi.platform.assistivecomunication.domain.model.events;

import java.time.LocalDateTime;

/** El acompañante no confirmó que entendió el mensaje: Red de Apoyo y Seguimiento genera una solicitud de ayuda. */
public record UnderstandingNotConfirmedEvent(Long selectionId, Long profileId, LocalDateTime occurredAt) {
}
