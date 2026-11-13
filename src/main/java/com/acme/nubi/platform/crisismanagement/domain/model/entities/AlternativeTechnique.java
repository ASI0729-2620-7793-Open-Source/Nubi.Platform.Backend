package com.acme.nubi.platform.crisismanagement.domain.model.entities;

import lombok.Getter;

/**
 * Técnica que se sugiere cuando un paso de contención no funciona.
 */
@Getter
public class AlternativeTechnique {

    private final Long id;
    private final String code;
    private final String title;
    private final String description;

    public AlternativeTechnique(Long id, String code, String title, String description) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("The alternative technique code is required.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("The alternative technique title is required.");
        }
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
    }
}
