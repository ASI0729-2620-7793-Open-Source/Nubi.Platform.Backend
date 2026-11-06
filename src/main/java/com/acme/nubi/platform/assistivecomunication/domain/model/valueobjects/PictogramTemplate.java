package com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects;

/** Plantilla de un pictograma básico que se carga en un tablero nuevo. */
public record PictogramTemplate(String label, String phrase, String imageUrl, CategoryType category, boolean favorite) {
}
