package com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryResource(@NotBlank @Size(max = 100) String name) {
}
