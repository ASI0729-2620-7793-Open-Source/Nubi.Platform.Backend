package com.acme.nubi.platform.assistivecomunication.domain.model.entities;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.CategoryType;
import lombok.Getter;

/** Categoría que agrupa los pictogramas de un tablero. */
@Getter
public class PictogramCategory {
    private Long id;
    private final String name;
    private final CategoryType type;
    private final Integer displayOrder;

    public PictogramCategory(Long id, String name, CategoryType type, Integer displayOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (type == null) {
            throw new IllegalArgumentException("El tipo de la categoría es obligatorio");
        }
        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException("El orden de la categoría no puede ser negativo");
        }
        this.id = id;
        this.name = name.trim();
        this.type = type;
        this.displayOrder = displayOrder;
    }

    public PictogramCategory(String name, CategoryType type, Integer displayOrder) {
        this(null, name, type, displayOrder);
    }
}
