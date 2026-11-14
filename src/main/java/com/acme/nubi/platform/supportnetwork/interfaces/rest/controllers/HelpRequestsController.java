package com.acme.nubi.platform.supportnetwork.interfaces.rest.controllers;

import com.acme.nubi.platform.supportnetwork.application.internal.commandservices.HelpRequestCommandServiceImpl;
import com.acme.nubi.platform.supportnetwork.application.internal.queryservices.HelpRequestQueryServiceImpl;
import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.HelpRequest;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.CancelHelpRequestCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.ConfirmHelpRequestCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetAllHelpRequestsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetHelpRequestByIdQuery;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.CreateHelpRequestResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.HelpRequestResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.transform.CreateHelpRequestCommandFromResourceAssembler;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.transform.HelpRequestResourceFromEntityAssembler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/help-requests")
public class HelpRequestsController {

    private final HelpRequestCommandServiceImpl commandService;
    private final HelpRequestQueryServiceImpl queryService;

    public HelpRequestsController(
            HelpRequestCommandServiceImpl commandService,
            HelpRequestQueryServiceImpl queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<HelpRequestResource> create(
            @RequestBody CreateHelpRequestResource resource) {
        HelpRequest helpRequest = commandService.handle(
                CreateHelpRequestCommandFromResourceAssembler.toCommandFromResource(resource));
        HelpRequestResource response = HelpRequestResourceFromEntityAssembler.toResourceFromEntity(helpRequest);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{helpRequestId}")
                .buildAndExpand(helpRequest.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{helpRequestId}")
    public ResponseEntity<HelpRequestResource> getById(@PathVariable Long helpRequestId) {
        return queryService.handle(new GetHelpRequestByIdQuery(helpRequestId))
                .map(HelpRequestResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<HelpRequestResource>> getAllByProfileId(
            @RequestParam Long profileId) {
        List<HelpRequestResource> resources = queryService
                .handle(new GetAllHelpRequestsByProfileIdQuery(profileId))
                .stream()
                .map(HelpRequestResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{helpRequestId}/confirmation")
    public ResponseEntity<HelpRequestResource> confirm(@PathVariable Long helpRequestId) {
        try {
            HelpRequest helpRequest = commandService.handle(new ConfirmHelpRequestCommand(helpRequestId));
            return ResponseEntity.ok(HelpRequestResourceFromEntityAssembler.toResourceFromEntity(helpRequest));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{helpRequestId}/cancellation")
    public ResponseEntity<HelpRequestResource> cancel(@PathVariable Long helpRequestId) {
        try {
            HelpRequest helpRequest = commandService.handle(new CancelHelpRequestCommand(helpRequestId));
            return ResponseEntity.ok(HelpRequestResourceFromEntityAssembler.toResourceFromEntity(helpRequest));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
