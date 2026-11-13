package com.acme.nubi.platform.selfregulation.application.internal.commandservices;

import com.acme.nubi.platform.selfregulation.application.internal.outboundservices.acl.ExternalProfileService;
import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmingResource;
import com.acme.nubi.platform.selfregulation.domain.model.commands.MarkFavoriteResourceCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.RemoveFavoriteResourceCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.SeedCalmingResourcesCommand;
import com.acme.nubi.platform.selfregulation.domain.model.entities.FavoriteResource;
import com.acme.nubi.platform.selfregulation.domain.model.errors.SelfRegulationErrors;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ResourceType;
import com.acme.nubi.platform.selfregulation.domain.repositories.CalmingResourceRepository;
import com.acme.nubi.platform.selfregulation.domain.repositories.FavoriteResourceRepository;
import com.acme.nubi.platform.selfregulation.domain.services.CalmingResourceCommandService;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class CalmingResourceCommandServiceImpl implements CalmingResourceCommandService {

    /** Catálogo inicial: los mismos estímulos que muestra la galería del frontend. */
    private static final List<CalmingResource> DEFAULT_CALMING_RESOURCES = List.of(
            new CalmingResource("FLOATING_BUBBLES", "Burbujas flotantes", ResourceType.VISUAL,
                    "stimuli/floating-bubbles.jpg", 10, true),
            new CalmingResource("OCEAN_SOUNDS", "Sonido del mar", ResourceType.AUDITORY,
                    "stimuli/ocean-sounds.jpg", 10, true),
            new CalmingResource("COLOR_WAVES", "Olas de color", ResourceType.VISUAL,
                    "stimuli/color-waves.jpg", 10, true),
            new CalmingResource("RELAXING_PIANO", "Piano relajante", ResourceType.AUDITORY,
                    "stimuli/relaxing-piano.jpg", 10, true),
            new CalmingResource("STARRY_SKY", "Cielo estrellado", ResourceType.VISUAL,
                    "stimuli/starry-sky.jpg", 10, true),
            new CalmingResource("GENTLE_RAIN", "Lluvia suave", ResourceType.AUDITORY,
                    "stimuli/gentle-rain.jpg", 10, true));

    private final CalmingResourceRepository calmingResourceRepository;
    private final FavoriteResourceRepository favoriteResourceRepository;
    private final ExternalProfileService externalProfileService;
    private final Clock clock;

    public CalmingResourceCommandServiceImpl(CalmingResourceRepository calmingResourceRepository,
                                             FavoriteResourceRepository favoriteResourceRepository,
                                             ExternalProfileService externalProfileService,
                                             Clock clock) {
        this.calmingResourceRepository = calmingResourceRepository;
        this.favoriteResourceRepository = favoriteResourceRepository;
        this.externalProfileService = externalProfileService;
        this.clock = clock;
    }

    @Override
    public Result<FavoriteResource, DomainError> handle(MarkFavoriteResourceCommand command) {
        var profileId = new ProfileId(command.profileId());
        if (!externalProfileService.existsProfile(profileId)) {
            return Result.failure(SelfRegulationErrors.profileNotFound(command.profileId()));
        }
        var resource = calmingResourceRepository.findById(command.resourceId());
        if (resource.isEmpty()) {
            return Result.failure(SelfRegulationErrors.calmingResourceNotFound(command.resourceId()));
        }
        if (!resource.get().isActive()) {
            return Result.failure(SelfRegulationErrors.calmingResourceUnavailable(command.resourceId()));
        }
        if (favoriteResourceRepository.existsByProfileIdAndResourceId(profileId, command.resourceId())) {
            return Result.failure(
                    SelfRegulationErrors.favoriteResourceAlreadyMarked(command.profileId(), command.resourceId()));
        }
        var favorite = new FavoriteResource(profileId, command.resourceId(), Instant.now(clock));
        return Result.success(favoriteResourceRepository.save(favorite));
    }

    @Override
    public Result<FavoriteResource, DomainError> handle(RemoveFavoriteResourceCommand command) {
        var favorite = favoriteResourceRepository.findByProfileIdAndResourceId(
                new ProfileId(command.profileId()), command.resourceId());
        if (favorite.isEmpty()) {
            return Result.failure(
                    SelfRegulationErrors.favoriteResourceNotFound(command.profileId(), command.resourceId()));
        }
        favoriteResourceRepository.delete(favorite.get());
        return Result.success(favorite.get());
    }

    @Override
    public void handle(SeedCalmingResourcesCommand command) {
        DEFAULT_CALMING_RESOURCES.stream()
                .filter(resource -> !calmingResourceRepository.existsByCode(resource.getCode()))
                .forEach(calmingResourceRepository::save);
    }
}
