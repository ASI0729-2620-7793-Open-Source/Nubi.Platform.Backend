package com.acme.nubi.platform.selfregulation.domain.model.aggregates;

import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ResourceType;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.SensoryProfileSnapshot;
import com.acme.nubi.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.Objects;

/**
 * Recurso de calma del catálogo: un estímulo que el usuario usa durante una sesión de calma.
 * El catálogo es compartido por todos los perfiles.
 */
@Getter
public class CalmingResource extends AbstractDomainAggregateRoot {

    public static final int MIN_INTENSITY = 1;

    /** Proporción de la intensidad máxima con la que empieza una sesión nueva. */
    private static final double DEFAULT_INTENSITY_RATIO = 0.6;

    private Long id;
    private final String code;
    private final String name;
    private final ResourceType type;
    private final String mediaUrl;
    private final int maxIntensity;
    private final boolean availableOffline;
    private final boolean active;

    public CalmingResource(String code, String name, ResourceType type, String mediaUrl, int maxIntensity,
                           boolean availableOffline) {
        this(null, code, name, type, mediaUrl, maxIntensity, availableOffline, true);
    }

    public CalmingResource(Long id, String code, String name, ResourceType type, String mediaUrl, int maxIntensity,
                           boolean availableOffline, boolean active) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("The calming resource code is required.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The calming resource name is required.");
        }
        if (maxIntensity < MIN_INTENSITY) {
            throw new IllegalArgumentException("The maximum intensity must be greater than zero.");
        }
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = Objects.requireNonNull(type, "The calming resource type is required.");
        this.mediaUrl = mediaUrl;
        this.maxIntensity = maxIntensity;
        this.availableOffline = availableOffline;
        this.active = active;
    }

    public boolean isVisual() {
        return type == ResourceType.VISUAL;
    }

    /**
     * Un recurso no es adecuado si estimula el sentido que el usuario tiene más sensible,
     * por ejemplo un sonido cuando la sensibilidad auditiva es alta.
     */
    public boolean isSuitableFor(SensoryProfileSnapshot snapshot) {
        if (!active) {
            return false;
        }
        return switch (type) {
            case AUDITORY -> !snapshot.hasHighAuditorySensitivity();
            case VISUAL -> !snapshot.hasHighVisualSensitivity();
            case GUIDED_BREATHING, DRAWING_CANVAS -> true;
        };
    }

    /** Ajusta una intensidad al rango del recurso: nunca menos de 1 ni más de su máximo. */
    public int clampIntensity(int requestedIntensity) {
        return Math.min(Math.max(MIN_INTENSITY, requestedIntensity), maxIntensity);
    }

    public int defaultIntensity() {
        return clampIntensity((int) Math.round(maxIntensity * DEFAULT_INTENSITY_RATIO));
    }
}
