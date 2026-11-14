package com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.SelectionStatus;
import jakarta.validation.constraints.NotNull;

/** Nuevo estado de una selección: DELIVERED, UNDERSTOOD o NOT_UNDERSTOOD. */
public record UpdateSelectionStatusResource(@NotNull SelectionStatus status) {
}
