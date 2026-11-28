package com.acme.nubi.platform.crisismanagement.interfaces.rest;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.CompleteStepCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.FinishSosSessionCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.RequestAlternativeCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.SkipStepCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.errors.CrisisManagementErrors;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetActionGuideByIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetSosSessionByIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.services.ActionGuideQueryService;
import com.acme.nubi.platform.crisismanagement.domain.services.SosSessionCommandService;
import com.acme.nubi.platform.crisismanagement.domain.services.SosSessionQueryService;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.UpdateSosSessionResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.UpdateStepStatusResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.SosSessionResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.StartSosSessionResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.transform.ActionGuideResourceFromEntityAssembler;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.transform.SosSessionResourceFromEntityAssembler;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.transform.StartSosSessionCommandFromResourceAssembler;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import com.acme.nubi.platform.shared.interfaces.rest.transform.ProblemDetailResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/api/v1/sos-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "SOS Sessions", description = "SOS Mode: guided sessions during a crisis (Crisis Management)")
public class SosSessionsController {

    private final SosSessionCommandService sosSessionCommandService;
    private final SosSessionQueryService sosSessionQueryService;
    private final ActionGuideQueryService actionGuideQueryService;

    public SosSessionsController(SosSessionCommandService sosSessionCommandService,
                                 SosSessionQueryService sosSessionQueryService,
                                 ActionGuideQueryService actionGuideQueryService) {
        this.sosSessionCommandService = sosSessionCommandService;
        this.sosSessionQueryService = sosSessionQueryService;
        this.actionGuideQueryService = actionGuideQueryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Activate the SOS mode",
            description = "Opens an SOS session with the current guide. Only one session can be in progress per profile.")
    public ResponseEntity<?> startSosSession(@RequestBody StartSosSessionResource resource) {
        var command = StartSosSessionCommandFromResourceAssembler.toCommandFromResource(resource);
        return sosSessionCommandService.handle(command).fold(
                session -> {
                    var location = ServletUriComponentsBuilder.fromCurrentRequest()
                            .path("/{sosSessionId}")
                            .buildAndExpand(session.getId())
                            .toUri();
                    return ResponseEntity.created(location)
                            .body(SosSessionResourceFromEntityAssembler.toResourceFromEntity(session));
                },
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @GetMapping("/{sosSessionId}")
    @Operation(summary = "Get an SOS session by id")
    public ResponseEntity<SosSessionResource> getSosSessionById(@PathVariable Long sosSessionId) {
        return sosSessionQueryService.handle(new GetSosSessionByIdQuery(sosSessionId))
                .map(SosSessionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{sosSessionId}/guide")
    @Operation(summary = "Get the guide of an SOS session",
            description = "The guide with its steps in the order personalized for the session's trigger.")
    public ResponseEntity<?> getSessionGuide(@PathVariable Long sosSessionId) {
        var session = sosSessionQueryService.handle(new GetSosSessionByIdQuery(sosSessionId));
        if (session.isEmpty()) {
            return ProblemDetailResponseAssembler.toResponseEntity(CrisisManagementErrors.sosSessionNotFound(sosSessionId));
        }
        var guide = actionGuideQueryService.handle(new GetActionGuideByIdQuery(session.get().getGuideId()));
        if (guide.isEmpty()) {
            return ProblemDetailResponseAssembler.toResponseEntity(
                    CrisisManagementErrors.actionGuideNotFound(session.get().getGuideId()));
        }
        var ordered = guide.get().orderedStepsFor(session.get().getTrigger());
        return ResponseEntity.ok(ActionGuideResourceFromEntityAssembler.toResourceFromEntity(guide.get(), ordered));
    }

    @PatchMapping(value = "/{sosSessionId}/steps/{stepId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update the status of a step",
            description = "status = COMPLETED records the step as done (repeating it has no effect). "
                    + "status = SKIPPED skips it; skipping a mandatory step requires confirmed = true.")
    public ResponseEntity<?> updateStep(@PathVariable Long sosSessionId, @PathVariable Long stepId,
                                        @RequestBody UpdateStepStatusResource resource) {
        var status = resource == null || resource.status() == null ? "" : resource.status().toUpperCase();
        return switch (status) {
            case "COMPLETED" -> toResponse(sosSessionCommandService.handle(new CompleteStepCommand(sosSessionId, stepId)));
            case "SKIPPED" -> toResponse(sosSessionCommandService.handle(
                    new SkipStepCommand(sosSessionId, stepId, Boolean.TRUE.equals(resource.confirmed()))));
            default -> ProblemDetailResponseAssembler.toResponseEntity(CrisisManagementErrors.invalidSosSessionData(
                    "El estado del paso debe ser COMPLETED o SKIPPED."));
        };
    }

    @PostMapping("/{sosSessionId}/steps/{stepId}/alternatives")
    @Operation(summary = "Create an alternative technique suggestion",
            description = "Suggests the alternative technique of the step when the caregiver cannot apply it.")
    public ResponseEntity<?> createAlternative(@PathVariable Long sosSessionId, @PathVariable Long stepId) {
        return sosSessionCommandService.handle(new RequestAlternativeCommand(sosSessionId, stepId)).fold(
                technique -> ResponseEntity.status(HttpStatus.CREATED).body(
                        ActionGuideResourceFromEntityAssembler.toTechniqueResource(stepId, technique)),
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @PatchMapping(value = "/{sosSessionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update the SOS session status",
            description = "status = FINISHED closes the session: if every step was handled it ends normally, "
                    + "otherwise it ends early. observedState and finalIntensity are optional.")
    public ResponseEntity<?> updateSosSession(@PathVariable Long sosSessionId,
                                              @RequestBody UpdateSosSessionResource resource) {
        if (resource == null || !"FINISHED".equalsIgnoreCase(resource.status())) {
            return ProblemDetailResponseAssembler.toResponseEntity(CrisisManagementErrors.invalidSosSessionData(
                    "El único cambio de estado permitido es FINISHED."));
        }
        return toResponse(sosSessionCommandService.handle(
                new FinishSosSessionCommand(sosSessionId, resource.observedState(), resource.finalIntensity())));
    }

    private static ResponseEntity<?> toResponse(Result<SosSession, DomainError> result) {
        return result.fold(
                session -> ResponseEntity.ok(SosSessionResourceFromEntityAssembler.toResourceFromEntity(session)),
                ProblemDetailResponseAssembler::toResponseEntity);
    }
}
