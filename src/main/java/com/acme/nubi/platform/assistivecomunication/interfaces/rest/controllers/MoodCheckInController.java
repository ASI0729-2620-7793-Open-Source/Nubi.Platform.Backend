package com.acme.nubi.platform.assistivecomunication.interfaces.rest.controllers;

import com.acme.nubi.platform.assistivecomunication.domain.model.commands.RegisterMoodCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetMoodCheckInsByProfileIdQuery;
import com.acme.nubi.platform.assistivecomunication.domain.services.CommunicationBoardCommandService;
import com.acme.nubi.platform.assistivecomunication.domain.services.CommunicationBoardQueryService;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.MoodCheckInResource;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.RegisterMoodResource;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform.MoodCheckInResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Check-in emocional del usuario (escala de cuatro niveles). */
@RestController
@RequestMapping(value = "/api/v1/profiles/{profileId}/communication-board/mood-check-ins",
        produces = "application/json")
@Tag(name = "Mood Check-ins", description = "Registro del estado de ánimo")
public class MoodCheckInController {
    private final CommunicationBoardCommandService commandService;
    private final CommunicationBoardQueryService queryService;

    public MoodCheckInController(CommunicationBoardCommandService commandService,
                                 CommunicationBoardQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(summary = "Registra el estado de ánimo del usuario")
    public ResponseEntity<MoodCheckInResource> registerMood(@PathVariable Long profileId,
                                                            @Valid @RequestBody RegisterMoodResource resource) {
        var checkIn = commandService.handle(new RegisterMoodCommand(profileId, resource.level()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MoodCheckInResourceFromEntityAssembler.toResourceFromEntity(checkIn));
    }

    @GetMapping
    @Operation(summary = "Historial de check-ins, del más reciente al más antiguo")
    public ResponseEntity<List<MoodCheckInResource>> getMoodCheckIns(@PathVariable Long profileId) {
        var checkIns = queryService.handle(new GetMoodCheckInsByProfileIdQuery(profileId));
        return ResponseEntity.ok(checkIns.stream()
                .map(MoodCheckInResourceFromEntityAssembler::toResourceFromEntity).toList());
    }
}
