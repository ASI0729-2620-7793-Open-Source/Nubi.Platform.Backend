package com.acme.nubi.platform.supportnetwork.interfaces.rest.controllers;

import com.acme.nubi.platform.supportnetwork.application.internal.commandservices.EpisodeReportCommandServiceImpl;
import com.acme.nubi.platform.supportnetwork.application.internal.queryservices.EpisodeReportQueryServiceImpl;
import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.EpisodeReport;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.MarkEpisodeReportAsSharedCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetAllEpisodeReportsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetEpisodeReportByIdQuery;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.CreateEpisodeReportResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.EpisodeReportResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.ShareEpisodeReportResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.transform.CreateEpisodeReportCommandFromResourceAssembler;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.transform.EpisodeReportResourceFromEntityAssembler;
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
@RequestMapping("/api/v1/episode-reports")
public class EpisodeReportsController {

    private final EpisodeReportCommandServiceImpl commandService;
    private final EpisodeReportQueryServiceImpl queryService;

    public EpisodeReportsController(
            EpisodeReportCommandServiceImpl commandService,
            EpisodeReportQueryServiceImpl queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<EpisodeReportResource> create(
            @RequestBody CreateEpisodeReportResource resource) {
        EpisodeReport episodeReport = commandService.handle(
                CreateEpisodeReportCommandFromResourceAssembler.toCommandFromResource(resource));
        EpisodeReportResource response =
                EpisodeReportResourceFromEntityAssembler.toResourceFromEntity(episodeReport);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{episodeReportId}")
                .buildAndExpand(episodeReport.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{episodeReportId}")
    public ResponseEntity<EpisodeReportResource> getById(@PathVariable Long episodeReportId) {
        return queryService.handle(new GetEpisodeReportByIdQuery(episodeReportId))
                .map(EpisodeReportResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EpisodeReportResource>> getAllByProfileId(
            @RequestParam Long profileId) {
        List<EpisodeReportResource> resources = queryService
                .handle(new GetAllEpisodeReportsByProfileIdQuery(profileId))
                .stream()
                .map(EpisodeReportResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{episodeReportId}/share")
    public ResponseEntity<EpisodeReportResource> markAsShared(
            @PathVariable Long episodeReportId,
            @RequestBody ShareEpisodeReportResource resource) {
        try {
            EpisodeReport episodeReport = commandService.handle(
                    new MarkEpisodeReportAsSharedCommand(episodeReportId, resource.sharedWith()));
            return ResponseEntity.ok(
                    EpisodeReportResourceFromEntityAssembler.toResourceFromEntity(episodeReport));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
