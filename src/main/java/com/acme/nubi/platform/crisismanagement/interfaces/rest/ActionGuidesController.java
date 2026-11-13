package com.acme.nubi.platform.crisismanagement.interfaces.rest;

import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetActionGuideByIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetActiveActionGuideQuery;
import com.acme.nubi.platform.crisismanagement.domain.services.ActionGuideQueryService;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.ActionGuideResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.transform.ActionGuideResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/action-guides", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Action Guides", description = "Guía de contención del Modo SOS (Gestión de Crisis)")
public class ActionGuidesController {

    private final ActionGuideQueryService actionGuideQueryService;

    public ActionGuidesController(ActionGuideQueryService actionGuideQueryService) {
        this.actionGuideQueryService = actionGuideQueryService;
    }

    @GetMapping("/active")
    @Operation(summary = "Get the active action guide",
            description = "La guía vigente con sus pasos en el orden original.")
    public ResponseEntity<ActionGuideResource> getActiveActionGuide() {
        return actionGuideQueryService.handle(new GetActiveActionGuideQuery())
                .map(ActionGuideResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{actionGuideId}")
    @Operation(summary = "Get an action guide by id")
    public ResponseEntity<ActionGuideResource> getActionGuideById(@PathVariable Long actionGuideId) {
        return actionGuideQueryService.handle(new GetActionGuideByIdQuery(actionGuideId))
                .map(ActionGuideResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
