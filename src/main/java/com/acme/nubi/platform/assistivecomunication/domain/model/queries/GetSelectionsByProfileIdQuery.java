package com.acme.nubi.platform.assistivecomunication.domain.model.queries;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.SelectionStatus;

/** Historial de selecciones del perfil; {@code status} es opcional y filtra por estado. */
public record GetSelectionsByProfileIdQuery(Long profileId, SelectionStatus status) {
}
