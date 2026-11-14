package com.acme.nubi.platform.supportnetwork.interfaces.rest.controllers;

import com.acme.nubi.platform.supportnetwork.application.internal.commandservices.EpisodeRecordCommandServiceImpl;
import com.acme.nubi.platform.supportnetwork.application.internal.queryservices.EpisodeRecordQueryServiceImpl;
import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.EpisodeRecord;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.AddEpisodeNoteCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetAllEpisodeRecordsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetEpisodeRecordByIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetEpisodeRecordsByProfileIdAndDateRangeQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetLatestEpisodeRecordByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetRecentEpisodeRecordsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.AddEpisodeNoteResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.CreateEpisodeRecordResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.EpisodeRecordResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.transform.CreateEpisodeRecordCommandFromResourceAssembler;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.transform.EpisodeRecordResourceFromEntityAssembler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/episode-records")
public class EpisodeRecordsController {

    private final EpisodeRecordCommandServiceImpl commandService;
    private final EpisodeRecordQueryServiceImpl queryService;

    public EpisodeRecordsController(
            EpisodeRecordCommandServiceImpl commandService,
            EpisodeRecordQueryServiceImpl queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<EpisodeRecordResource> create(
            @RequestBody CreateEpisodeRecordResource resource) {
        EpisodeRecord episodeRecord = commandService.handle(
                CreateEpisodeRecordCommandFromResourceAssembler.toCommandFromResource(resource));
        EpisodeRecordResource response =
                EpisodeRecordResourceFromEntityAssembler.toResourceFromEntity(episodeRecord);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{episodeRecordId}")
                .buildAndExpand(episodeRecord.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{episodeRecordId}")
    public ResponseEntity<EpisodeRecordResource> getById(@PathVariable Long episodeRecordId) {
        return queryService.handle(new GetEpisodeRecordByIdQuery(episodeRecordId))
                .map(EpisodeRecordResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Episodios del perfil. Con startDate y endDate (ISO, opcionales) se filtran por rango de
     * fechas: el filtro va en query params, no en una ruta aparte.
     */
    @GetMapping
    public ResponseEntity<List<EpisodeRecordResource>> getAllByProfileId(
            @RequestParam Long profileId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        var records = startDate != null && endDate != null
                ? queryService.handle(new GetEpisodeRecordsByProfileIdAndDateRangeQuery(profileId, startDate, endDate))
                : queryService.handle(new GetAllEpisodeRecordsByProfileIdQuery(profileId));
        List<EpisodeRecordResource> resources = records.stream()
                .map(EpisodeRecordResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/latest")
    public ResponseEntity<EpisodeRecordResource> getLatestByProfileId(
            @RequestParam Long profileId) {
        return queryService.handle(new GetLatestEpisodeRecordByProfileIdQuery(profileId))
                .map(EpisodeRecordResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<EpisodeRecordResource>> getRecentByProfileId(
            @RequestParam Long profileId,
            @RequestParam(required = false) Integer limit) {
        List<EpisodeRecordResource> resources = queryService
                .handle(new GetRecentEpisodeRecordsByProfileIdQuery(profileId, limit))
                .stream()
                .map(EpisodeRecordResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{episodeRecordId}/notes")
    public ResponseEntity<EpisodeRecordResource> addNote(
            @PathVariable Long episodeRecordId,
            @RequestBody AddEpisodeNoteResource resource) {
        try {
            EpisodeRecord episodeRecord = commandService.handle(
                    new AddEpisodeNoteCommand(episodeRecordId, resource.notes()));
            return ResponseEntity.ok(
                    EpisodeRecordResourceFromEntityAssembler.toResourceFromEntity(episodeRecord));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
