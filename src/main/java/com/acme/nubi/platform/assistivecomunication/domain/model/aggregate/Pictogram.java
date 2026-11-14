package com.acme.nubi.platform.assistivecomunication.domain.model.aggregate;

import lombok.Getter;

import java.util.List;
import java.util.Locale;

/**
 * Pictograma del tablero CAA: lo que el usuario toca para comunicar algo.
 * Tiene la etiqueta que se muestra, la frase que se reproduce en voz alta, su imagen
 * y un audio opcional.
 */
@Getter
public class Pictogram {
    private static final List<String> SUPPORTED_IMAGE_EXTENSIONS =
            List.of(".png", ".jpg", ".jpeg", ".gif", ".svg", ".webp");

    private Long id;
    private Long categoryId;
    private final String label;
    private final String phrase;
    private final String imageUrl;
    private final String audioUrl;
    private boolean favorite;
    private final boolean custom;
    private final Integer displayOrder;

    public Pictogram(Long id, Long categoryId, String label, String phrase, String imageUrl, String audioUrl,
                     boolean favorite, boolean custom, Integer displayOrder) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("La etiqueta del pictograma es obligatoria");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("El pictograma debe pertenecer a una categoría");
        }
        if (!isSupportedImage(imageUrl)) {
            throw new IllegalArgumentException(
                    "La imagen no es válida: los formatos soportados son " + String.join(", ", SUPPORTED_IMAGE_EXTENSIONS));
        }
        this.id = id;
        this.categoryId = categoryId;
        this.label = label.trim();
        this.phrase = (phrase == null || phrase.isBlank()) ? this.label : phrase.trim();
        this.imageUrl = imageUrl.trim();
        this.audioUrl = (audioUrl == null || audioUrl.isBlank()) ? null : audioUrl.trim();
        this.favorite = favorite;
        this.custom = custom;
        this.displayOrder = displayOrder == null ? 0 : displayOrder;
    }

    public boolean hasAudio() {
        return audioUrl != null;
    }

    public void markAsFavorite() {
        this.favorite = true;
    }

    public void unmarkAsFavorite() {
        this.favorite = false;
    }

    public void assignToCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        this.categoryId = categoryId;
    }

    /** Acepta una ruta o URL que termine en un formato de imagen soportado, o una imagen en data URI. */
    private static boolean isSupportedImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }
        String normalized = imageUrl.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("data:image/")) {
            return true;
        }
        int queryStart = normalized.indexOf('?');
        String path = queryStart >= 0 ? normalized.substring(0, queryStart) : normalized;
        return SUPPORTED_IMAGE_EXTENSIONS.stream().anyMatch(path::endsWith);
    }
}
