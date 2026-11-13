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
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.FinishSosSessionResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.SkipStepResource;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/api/v1/sos-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "SOS Sessions", description = "Modo SOS: sesiones guiadas durante una crisis (Gestión de Crisis)")
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
            description = "Abre una sesión SOS con la guía vigente. Solo puede haber una sesión en curso por perfil.")
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
            description = "La guía con los pasos en el orden personalizado según el detonante de la sesión.")
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

    @PostMapping("/{sosSessionId}/steps/{stepId}/complete")
    @Operation(summary = "Complete a step",
            description = "Registra el paso como completado y avanza al siguiente. Repetirlo no tiene efecto.")
    public ResponseEntity<?> completeStep(@PathVariable Long sosSessionId, @PathVariable Long stepId) {
        return toResponse(sosSessionCommandService.handle(new CompleteStepCommand(sosSessionId, stepId)));
    }

    @PostMapping("/{sosSessionId}/steps/{stepId}/skip")
    @Operation(summary = "Skip a step",
            description = "Omitir un paso obligatorio exige confirmed = true.")
    public ResponseEntity<?> skipStep(@PathVariable Long sosSessionId, @PathVariable Long stepId,
                                      @RequestBody(required = false) SkipStepResource resource) {
        var confirmed = resource != null && Boolean.TRUE.equals(resource.confirmed());
        return toResponse(sosSessionCommandService.handle(new SkipStepCommand(sosSessionId, stepId, confirmed)));
    }

    @PostMapping("/{sosSessionId}/steps/{stepId}/alternative")
    @Operation(summary = "Request an alternative technique",
            description = "Sugiere la técnica alternativa del paso cuando el cuidador no puede aplicarlo.")
    public ResponseEntity<?> requestAlternative(@PathVariable Long sosSessionId, @PathVariable Long stepId) {
        return sosSessionCommandService.handle(new RequestAlternativeCommand(sosSessionId, stepId)).fold(
                technique -> ResponseEntity.ok(
                        ActionGuideResourceFromEntityAssembler.toTechniqueResource(stepId, technique)),
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @PostMapping("/{sosSessionId}/finish")
    @Operation(summary = "Finish the SOS session",
            description = "Con todos los pasos atendidos termina la sesión; si faltan pasos, la termina antes de tiempo. "
                    + "El estado observado y la intensidad final son opcionales.")
    public ResponseEntity<?> finishSosSession(@PathVariable Long sosSessionId,
                                              @RequestBody(required = false) FinishSosSessionResource resource) {
        var observedState = resource == null ? null : resource.observedState();
        var finalIntensity = resource == null ? null : resource.finalIntensity();
        return toResponse(sosSessionCommandService.handle(
                new FinishSosSessionCommand(sosSessionId, observedState, finalIntensity)));
    }

    private static ResponseEntity<?> toResponse(Result<SosSession, DomainError> result) {
        return result.fold(
                session -> ResponseEntity.ok(SosSessionResourceFromEntityAssembler.toResourceFromEntity(session)),
                ProblemDetailResponseAssembler::toResponseEntity);
    }
}
