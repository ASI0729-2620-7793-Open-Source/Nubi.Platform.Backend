package com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

public record MoveToCategoryResource(@NotNull Long categoryId) {
}
