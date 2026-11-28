package com.acme.nubi.platform.crisismanagement.interfaces.rest;

import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
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
@Tag(name = "Action Guides", description = "SOS Mode containment guide (Crisis Management)")
public class ActionGuidesController {

    private final ActionGuideQueryService actionGuideQueryService;

    public ActionGuidesController(ActionGuideQueryService actionGuideQueryService) {
        this.actionGuideQueryService = actionGuideQueryService;
    }

    @GetMapping("/active")
    @Operation(summary = "Get the active action guide",
            description = "The current guide with its steps in their original order.")
    public ResponseEntity<ActionGuideResource> getActiveActionGuide() {
        return actionGuideQueryService.handle(new GetActiveActionGuideQuery())
                .map(ActionGuideResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("The requested resource does not exist."));
    }

    @GetMapping("/{actionGuideId}")
    @Operation(summary = "Get an action guide by id")
    public ResponseEntity<ActionGuideResource> getActionGuideById(@PathVariable Long actionGuideId) {
        return actionGuideQueryService.handle(new GetActionGuideByIdQuery(actionGuideId))
                .map(ActionGuideResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("The requested resource does not exist."));
    }
}
