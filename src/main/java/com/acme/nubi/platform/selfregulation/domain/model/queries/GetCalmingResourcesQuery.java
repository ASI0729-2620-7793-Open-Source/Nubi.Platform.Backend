package com.acme.nubi.platform.selfregulation.domain.model.queries;

import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ResourceType;

/**
 * Catálogo de recursos de calma vigentes.
 *
 * @param profileId si se indica, solo devuelve los recursos adecuados para su perfil sensorial
 * @param type      si se indica, filtra por tipo de estímulo
 */
public record GetCalmingResourcesQuery(Long profileId, ResourceType type) {

    public GetCalmingResourcesQuery {
        if (profileId != null && profileId <= 0) {
            throw new IllegalArgumentException("The profile id must be a positive number.");
        }
    }
}
