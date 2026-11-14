package com.acme.nubi.platform.selfregulation.interfaces.rest;

import com.acme.nubi.platform.selfregulation.domain.model.commands.RemoveFavoriteResourceCommand;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetFavoriteResourcesByProfileIdQuery;
import com.acme.nubi.platform.selfregulation.domain.services.CalmingResourceCommandService;
import com.acme.nubi.platform.selfregulation.domain.services.CalmingResourceQueryService;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.MarkFavoriteResourceResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.transform.FavoriteResourceResourceFromEntityAssembler;
import com.acme.nubi.platform.selfregulation.interfaces.rest.transform.MarkFavoriteResourceCommandFromResourceAssembler;
import com.acme.nubi.platform.shared.interfaces.rest.transform.ProblemDetailResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/profiles/{profileId}/favorite-resources", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Calming Resources", description = "Catálogo de recursos de calma (Autorregulación)")
public class ProfileFavoriteResourcesController {

    private final CalmingResourceCommandService calmingResourceCommandService;
    private final CalmingResourceQueryService calmingResourceQueryService;

    public ProfileFavoriteResourcesController(CalmingResourceCommandService calmingResourceCommandService,
                                              CalmingResourceQueryService calmingResourceQueryService) {
        this.calmingResourceCommandService = calmingResourceCommandService;
        this.calmingResourceQueryService = calmingResourceQueryService;
    }

    @GetMapping
    @Operation(summary = "Get the favorite calming resources of a profile")
    public ResponseEntity<?> getFavoriteResources(@PathVariable Long profileId) {
        var result = calmingResourceQueryService.handle(new GetFavoriteResourcesByProfileIdQuery(profileId));
        return result.fold(
                favorites -> ResponseEntity.ok(favorites.stream()
                        .map(FavoriteResourceResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()),
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Mark a calming resource as favorite")
    public ResponseEntity<?> markFavoriteResource(@PathVariable Long profileId,
                                                  @RequestBody MarkFavoriteResourceResource resource) {
        var command = MarkFavoriteResourceCommandFromResourceAssembler.toCommandFromResource(profileId, resource);
        return calmingResourceCommandService.handle(command).fold(
                favorite -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(FavoriteResourceResourceFromEntityAssembler.toResourceFromEntity(favorite)),
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @DeleteMapping("/{resourceId}")
    @Operation(summary = "Remove a calming resource from the favorites")
    public ResponseEntity<?> removeFavoriteResource(@PathVariable Long profileId, @PathVariable Long resourceId) {
        return calmingResourceCommandService.handle(new RemoveFavoriteResourceCommand(profileId, resourceId)).fold(
                favorite -> ResponseEntity.noContent().build(),
                ProblemDetailResponseAssembler::toResponseEntity);
    }
}
