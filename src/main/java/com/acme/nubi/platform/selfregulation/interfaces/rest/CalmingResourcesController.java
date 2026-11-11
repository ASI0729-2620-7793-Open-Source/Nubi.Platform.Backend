package com.acme.nubi.platform.selfregulation.interfaces.rest;

import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmingResourceByIdQuery;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmingResourcesQuery;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ResourceType;
import com.acme.nubi.platform.selfregulation.domain.services.CalmingResourceQueryService;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.CalmingResourceResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.transform.CalmingResourceResourceFromEntityAssembler;
import com.acme.nubi.platform.shared.interfaces.rest.transform.ProblemDetailResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/calming-resources", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Calming Resources", description = "Catálogo de recursos de calma (Autorregulación)")
public class CalmingResourcesController {

    private final CalmingResourceQueryService calmingResourceQueryService;

    public CalmingResourcesController(CalmingResourceQueryService calmingResourceQueryService) {
        this.calmingResourceQueryService = calmingResourceQueryService;
    }

    @GetMapping
    @Operation(summary = "Get calming resources",
            description = "Lista los recursos de calma vigentes. Con profileId, solo los adecuados para su perfil sensorial; con type, solo los de ese tipo.")
    public ResponseEntity<?> getCalmingResources(@RequestParam(required = false) Long profileId,
                                                 @RequestParam(required = false) ResourceType type) {
        var result = calmingResourceQueryService.handle(new GetCalmingResourcesQuery(profileId, type));
        return result.fold(
                resources -> ResponseEntity.ok(resources.stream()
                        .map(CalmingResourceResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()),
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @GetMapping("/{calmingResourceId}")
    @Operation(summary = "Get a calming resource by id")
    public ResponseEntity<CalmingResourceResource> getCalmingResourceById(@PathVariable Long calmingResourceId) {
        return calmingResourceQueryService.handle(new GetCalmingResourceByIdQuery(calmingResourceId))
                .map(CalmingResourceResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
