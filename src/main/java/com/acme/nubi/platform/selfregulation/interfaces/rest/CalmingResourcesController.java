package com.acme.nubi.platform.selfregulation.interfaces.rest;

import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
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
@Tag(name = "Calming Resources", description = "Calming resource catalog (Self-Regulation)")
public class CalmingResourcesController {

    private final CalmingResourceQueryService calmingResourceQueryService;

    public CalmingResourcesController(CalmingResourceQueryService calmingResourceQueryService) {
        this.calmingResourceQueryService = calmingResourceQueryService;
    }

    @GetMapping
    @Operation(summary = "Get calming resources",
            description = "Lists the active calming resources. With profileId, only those suited to its sensory profile; with type, only that type.")
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
                .orElseThrow(() -> new ResourceNotFoundException("The requested resource does not exist."));
    }
}
