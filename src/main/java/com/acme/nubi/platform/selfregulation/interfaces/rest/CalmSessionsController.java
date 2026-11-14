package com.acme.nubi.platform.selfregulation.interfaces.rest;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmSession;
import com.acme.nubi.platform.selfregulation.domain.model.commands.AdjustIntensityCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.AdvanceCalmTimerCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.ChangeLowStimulationCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.FinishCalmSessionCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.PauseCalmSessionCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.PauseCalmTimerCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.ResumeCalmSessionCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.ResumeCalmTimerCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.SelectCalmingResourceCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.StartCalmTimerCommand;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmSessionByIdQuery;
import com.acme.nubi.platform.selfregulation.domain.services.CalmSessionCommandService;
import com.acme.nubi.platform.selfregulation.domain.services.CalmSessionQueryService;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.AdjustIntensityResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.AdvanceCalmTimerResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.CalmSessionResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.ChangeLowStimulationResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.SelectCalmingResourceResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.StartCalmSessionResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.resources.StartCalmTimerResource;
import com.acme.nubi.platform.selfregulation.interfaces.rest.transform.CalmSessionResourceFromEntityAssembler;
import com.acme.nubi.platform.selfregulation.interfaces.rest.transform.StartCalmSessionCommandFromResourceAssembler;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import com.acme.nubi.platform.shared.interfaces.rest.transform.ProblemDetailResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/api/v1/calm-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Calm Sessions", description = "Sesiones de calma y temporizador de calma (Autorregulación)")
public class CalmSessionsController {

    private final CalmSessionCommandService calmSessionCommandService;
    private final CalmSessionQueryService calmSessionQueryService;

    public CalmSessionsController(CalmSessionCommandService calmSessionCommandService,
                                  CalmSessionQueryService calmSessionQueryService) {
        this.calmSessionCommandService = calmSessionCommandService;
        this.calmSessionQueryService = calmSessionQueryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start a calm session",
            description = "Abre una sesión de calma para un perfil, con o sin un recurso elegido.")
    public ResponseEntity<?> startCalmSession(@RequestBody StartCalmSessionResource resource) {
        var command = StartCalmSessionCommandFromResourceAssembler.toCommandFromResource(resource);
        return calmSessionCommandService.handle(command).fold(
                session -> {
                    var location = ServletUriComponentsBuilder.fromCurrentRequest()
                            .path("/{calmSessionId}")
                            .buildAndExpand(session.getId())
                            .toUri();
                    return ResponseEntity.created(location)
                            .body(CalmSessionResourceFromEntityAssembler.toResourceFromEntity(session));
                },
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @GetMapping("/{calmSessionId}")
    @Operation(summary = "Get a calm session by id")
    public ResponseEntity<CalmSessionResource> getCalmSessionById(@PathVariable Long calmSessionId) {
        return calmSessionQueryService.handle(new GetCalmSessionByIdQuery(calmSessionId))
                .map(CalmSessionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{calmSessionId}/resource", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Select the calming resource of a session")
    public ResponseEntity<?> selectCalmingResource(@PathVariable Long calmSessionId,
                                                   @RequestBody SelectCalmingResourceResource resource) {
        return toResponse(calmSessionCommandService.handle(
                new SelectCalmingResourceCommand(calmSessionId, resource.resourceId())));
    }

    @PutMapping(value = "/{calmSessionId}/intensity", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Adjust the stimulus intensity",
            description = "Una intensidad mayor al máximo del recurso se mantiene en el máximo.")
    public ResponseEntity<?> adjustIntensity(@PathVariable Long calmSessionId,
                                             @RequestBody AdjustIntensityResource resource) {
        return toResponse(calmSessionCommandService.handle(
                new AdjustIntensityCommand(calmSessionId, resource.intensity())));
    }

    @PutMapping(value = "/{calmSessionId}/low-stimulation", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Activate or deactivate the low stimulation mode")
    public ResponseEntity<?> changeLowStimulation(@PathVariable Long calmSessionId,
                                                  @RequestBody ChangeLowStimulationResource resource) {
        return toResponse(calmSessionCommandService.handle(
                new ChangeLowStimulationCommand(calmSessionId, resource.active())));
    }

    @PostMapping("/{calmSessionId}/pause")
    @Operation(summary = "Pause the calm session")
    public ResponseEntity<?> pauseCalmSession(@PathVariable Long calmSessionId) {
        return toResponse(calmSessionCommandService.handle(new PauseCalmSessionCommand(calmSessionId)));
    }

    @PostMapping("/{calmSessionId}/resume")
    @Operation(summary = "Resume the calm session")
    public ResponseEntity<?> resumeCalmSession(@PathVariable Long calmSessionId) {
        return toResponse(calmSessionCommandService.handle(new ResumeCalmSessionCommand(calmSessionId)));
    }

    @PostMapping(value = "/{calmSessionId}/timer", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start the calm timer")
    public ResponseEntity<?> startCalmTimer(@PathVariable Long calmSessionId,
                                            @RequestBody StartCalmTimerResource resource) {
        return toResponse(calmSessionCommandService.handle(
                new StartCalmTimerCommand(calmSessionId, resource.durationMinutes())));
    }

    @PostMapping("/{calmSessionId}/timer/pause")
    @Operation(summary = "Pause the calm timer")
    public ResponseEntity<?> pauseCalmTimer(@PathVariable Long calmSessionId) {
        return toResponse(calmSessionCommandService.handle(new PauseCalmTimerCommand(calmSessionId)));
    }

    @PostMapping("/{calmSessionId}/timer/resume")
    @Operation(summary = "Resume the calm timer")
    public ResponseEntity<?> resumeCalmTimer(@PathVariable Long calmSessionId) {
        return toResponse(calmSessionCommandService.handle(new ResumeCalmTimerCommand(calmSessionId)));
    }

    @PostMapping(value = "/{calmSessionId}/timer/ticks", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Advance the calm timer",
            description = "Descuenta los segundos transcurridos. Si el temporizador llega a cero con la sesión abierta, shouldSuggestHelp es verdadero.")
    public ResponseEntity<?> advanceCalmTimer(@PathVariable Long calmSessionId,
                                              @RequestBody AdvanceCalmTimerResource resource) {
        return toResponse(calmSessionCommandService.handle(
                new AdvanceCalmTimerCommand(calmSessionId, resource.elapsedSeconds())));
    }

    @PostMapping("/{calmSessionId}/finish")
    @Operation(summary = "Finish the calm session")
    public ResponseEntity<?> finishCalmSession(@PathVariable Long calmSessionId) {
        return toResponse(calmSessionCommandService.handle(new FinishCalmSessionCommand(calmSessionId)));
    }

    private static ResponseEntity<?> toResponse(Result<CalmSession, DomainError> result) {
        return result.fold(
                session -> ResponseEntity.ok(CalmSessionResourceFromEntityAssembler.toResourceFromEntity(session)),
                ProblemDetailResponseAssembler::toResponseEntity);
    }
}
