package com.acme.nubi.platform.selfregulation.domain.services;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmingResource;
import com.acme.nubi.platform.selfregulation.domain.model.entities.FavoriteResource;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmingResourceByIdQuery;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmingResourcesQuery;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetFavoriteResourcesByProfileIdQuery;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;

import java.util.List;
import java.util.Optional;

public interface CalmingResourceQueryService {
    Result<List<CalmingResource>, DomainError> handle(GetCalmingResourcesQuery query);

    Optional<CalmingResource> handle(GetCalmingResourceByIdQuery query);

    Result<List<FavoriteResource>, DomainError> handle(GetFavoriteResourcesByProfileIdQuery query);
}
