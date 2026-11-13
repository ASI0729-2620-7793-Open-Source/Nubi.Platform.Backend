package com.acme.nubi.platform.selfregulation.domain.services;

import com.acme.nubi.platform.selfregulation.domain.model.commands.MarkFavoriteResourceCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.RemoveFavoriteResourceCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.SeedCalmingResourcesCommand;
import com.acme.nubi.platform.selfregulation.domain.model.entities.FavoriteResource;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;

public interface CalmingResourceCommandService {
    Result<FavoriteResource, DomainError> handle(MarkFavoriteResourceCommand command);

    /** Devuelve el favorito eliminado. */
    Result<FavoriteResource, DomainError> handle(RemoveFavoriteResourceCommand command);

    void handle(SeedCalmingResourcesCommand command);
}
