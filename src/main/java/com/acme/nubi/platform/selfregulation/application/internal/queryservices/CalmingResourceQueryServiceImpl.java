package com.acme.nubi.platform.selfregulation.application.internal.queryservices;

import com.acme.nubi.platform.selfregulation.application.internal.outboundservices.acl.ExternalProfileService;
import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmingResource;
import com.acme.nubi.platform.selfregulation.domain.model.entities.FavoriteResource;
import com.acme.nubi.platform.selfregulation.domain.model.errors.SelfRegulationErrors;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmingResourceByIdQuery;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmingResourcesQuery;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetFavoriteResourcesByProfileIdQuery;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.domain.repositories.CalmingResourceRepository;
import com.acme.nubi.platform.selfregulation.domain.repositories.FavoriteResourceRepository;
import com.acme.nubi.platform.selfregulation.domain.services.CalmingResourceQueryService;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CalmingResourceQueryServiceImpl implements CalmingResourceQueryService {

    private final CalmingResourceRepository calmingResourceRepository;
    private final FavoriteResourceRepository favoriteResourceRepository;
    private final ExternalProfileService externalProfileService;

    public CalmingResourceQueryServiceImpl(CalmingResourceRepository calmingResourceRepository,
                                           FavoriteResourceRepository favoriteResourceRepository,
                                           ExternalProfileService externalProfileService) {
        this.calmingResourceRepository = calmingResourceRepository;
        this.favoriteResourceRepository = favoriteResourceRepository;
        this.externalProfileService = externalProfileService;
    }

    /** Con un perfil, solo devuelve los recursos adecuados para su perfil sensorial. */
    @Override
    public Result<List<CalmingResource>, DomainError> handle(GetCalmingResourcesQuery query) {
        var resources = query.type() == null
                ? calmingResourceRepository.findAllActive()
                : calmingResourceRepository.findAllActiveByType(query.type());
        if (query.profileId() == null) {
            return Result.success(resources);
        }

        var profileId = new ProfileId(query.profileId());
        if (!externalProfileService.existsProfile(profileId)) {
            return Result.failure(SelfRegulationErrors.profileNotFound(query.profileId()));
        }
        var suitableResources = externalProfileService.fetchSensoryProfile(profileId)
                .map(snapshot -> resources.stream().filter(resource -> resource.isSuitableFor(snapshot)).toList())
                .orElse(resources);
        return Result.success(suitableResources);
    }

    @Override
    public Optional<CalmingResource> handle(GetCalmingResourceByIdQuery query) {
        return calmingResourceRepository.findById(query.calmingResourceId());
    }

    @Override
    public Result<List<FavoriteResource>, DomainError> handle(GetFavoriteResourcesByProfileIdQuery query) {
        var profileId = new ProfileId(query.profileId());
        if (!externalProfileService.existsProfile(profileId)) {
            return Result.failure(SelfRegulationErrors.profileNotFound(query.profileId()));
        }
        return Result.success(favoriteResourceRepository.findAllByProfileId(profileId));
    }
}
