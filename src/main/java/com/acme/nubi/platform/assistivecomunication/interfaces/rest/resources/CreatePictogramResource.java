package com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo para crear un pictograma personalizado. Si no se indica la frase, se usa la etiqueta. */
public record CreatePictogramResource(
        @NotBlank @Size(max = 100) String label,
        @Size(max = 255) String phrase,
        @NotBlank @Size(max = 500) String imageUrl,
        @Size(max = 500) String audioUrl,
        @NotNull Long categoryId) {
}
