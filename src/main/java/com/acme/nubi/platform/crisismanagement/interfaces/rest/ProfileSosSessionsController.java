package com.acme.nubi.platform.crisismanagement.interfaces.rest;

import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetActiveSosSessionByProfileIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetSosSessionsByProfileIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.services.SosSessionQueryService;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.SosSessionResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.transform.SosSessionResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/profiles/{profileId}/sos-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "SOS Sessions", description = "SOS Mode: guided sessions during a crisis (Crisis Management)")
public class ProfileSosSessionsController {

    private final SosSessionQueryService sosSessionQueryService;

    public ProfileSosSessionsController(SosSessionQueryService sosSessionQueryService) {
        this.sosSessionQueryService = sosSessionQueryService;
    }

    @GetMapping
    @Operation(summary = "Get the SOS sessions of a profile", description = "From newest to oldest.")
    public ResponseEntity<List<SosSessionResource>> getSosSessions(@PathVariable Long profileId) {
        var sessions = sosSessionQueryService.handle(new GetSosSessionsByProfileIdQuery(profileId));
        return ResponseEntity.ok(sessions.stream()
                .map(SosSessionResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @GetMapping("/in-progress")
    @Operation(summary = "Get the SOS session in progress of a profile",
            description = "Responds 404 if the profile has no session in progress.")
    public ResponseEntity<SosSessionResource> getInProgressSosSession(@PathVariable Long profileId) {
        return sosSessionQueryService.handle(new GetActiveSosSessionByProfileIdQuery(profileId))
                .map(SosSessionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
