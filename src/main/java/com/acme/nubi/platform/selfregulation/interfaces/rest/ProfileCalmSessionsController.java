package com.acme.nubi.platform.selfregulation.interfaces.rest;

import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmSessionsByProfileIdQuery;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetLatestCalmSessionByProfileIdQuery;
import com.acme.nubi.platform.selfregulation.domain.services.CalmSessionQueryService;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.CalmSessionResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.transform.CalmSessionResourceFromEntityAssembler;
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
@RequestMapping(value = "/api/v1/profiles/{profileId}/calm-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Calm Sessions", description = "Calm sessions and calm timer (Self-Regulation)")
public class ProfileCalmSessionsController {

    private final CalmSessionQueryService calmSessionQueryService;

    public ProfileCalmSessionsController(CalmSessionQueryService calmSessionQueryService) {
        this.calmSessionQueryService = calmSessionQueryService;
    }

    @GetMapping
    @Operation(summary = "Get the calm sessions of a profile", description = "From newest to oldest.")
    public ResponseEntity<List<CalmSessionResource>> getCalmSessions(@PathVariable Long profileId) {
        var sessions = calmSessionQueryService.handle(new GetCalmSessionsByProfileIdQuery(profileId));
        return ResponseEntity.ok(sessions.stream()
                .map(CalmSessionResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @GetMapping("/latest")
    @Operation(summary = "Get the latest calm session of a profile")
    public ResponseEntity<CalmSessionResource> getLatestCalmSession(@PathVariable Long profileId) {
        return calmSessionQueryService.handle(new GetLatestCalmSessionByProfileIdQuery(profileId))
                .map(CalmSessionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
