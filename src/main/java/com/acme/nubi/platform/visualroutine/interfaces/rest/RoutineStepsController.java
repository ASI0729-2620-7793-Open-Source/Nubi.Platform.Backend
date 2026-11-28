package com.acme.nubi.platform.visualroutine.interfaces.rest;

import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import com.acme.nubi.platform.shared.interfaces.rest.transform.ProblemDetailResponseAssembler;
import com.acme.nubi.platform.visualroutine.application.internal.RoutineService;
import com.acme.nubi.platform.visualroutine.domain.model.aggregates.RoutineStep;
import com.acme.nubi.platform.visualroutine.interfaces.rest.resources.RoutineResources.ReorderRoutineResource;
import com.acme.nubi.platform.visualroutine.interfaces.rest.resources.RoutineResources.RoutineStepResource;
import com.acme.nubi.platform.visualroutine.interfaces.rest.resources.RoutineResources.SaveRoutineStepResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/profiles/{profileId}/routine-steps", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Visual Routine", description = "The child's visual routine for the day")
public class RoutineStepsController {

    private final RoutineService routineService;

    public RoutineStepsController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @GetMapping
    @Operation(summary = "Get the routine of the day",
            description = "If the child has no routine yet, the suggested routine is created.")
    public ResponseEntity<?> getRoutine(@PathVariable Long profileId) {
        var today = routineService.today();
        return routineService.getRoutine(profileId).fold(
                steps -> ResponseEntity.ok(steps.stream().map(step -> RoutineStepResource.from(step, today)).toList()),
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a step to the routine")
    public ResponseEntity<?> addStep(@PathVariable Long profileId, @RequestBody SaveRoutineStepResource resource) {
        return routineService.addStep(profileId, resource.title(), resource.icon(), resource.scheduledTime()).fold(
                step -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(RoutineStepResource.from(step, routineService.today())),
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @PutMapping(value = "/{stepId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Edit a step")
    public ResponseEntity<?> updateStep(@PathVariable Long profileId, @PathVariable Long stepId,
                                        @RequestBody SaveRoutineStepResource resource) {
        return toResponse(routineService.updateStep(
                profileId, stepId, resource.title(), resource.icon(), resource.scheduledTime()));
    }

    @PutMapping(value = "/order", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Reorder the routine")
    public ResponseEntity<?> reorder(@PathVariable Long profileId, @RequestBody ReorderRoutineResource resource) {
        var today = routineService.today();
        return routineService.reorder(profileId, resource.order()).fold(
                steps -> ResponseEntity.ok(steps.stream().map(step -> RoutineStepResource.from(step, today)).toList()),
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @DeleteMapping("/{stepId}")
    @Operation(summary = "Remove a step")
    public ResponseEntity<?> removeStep(@PathVariable Long profileId, @PathVariable Long stepId) {
        return routineService.remove(profileId, stepId).fold(
                removed -> ResponseEntity.noContent().build(),
                ProblemDetailResponseAssembler::toResponseEntity);
    }

    @PostMapping("/{stepId}/completion")
    @Operation(summary = "Mark a step as done today")
    public ResponseEntity<?> complete(@PathVariable Long profileId, @PathVariable Long stepId) {
        return toResponse(routineService.complete(profileId, stepId));
    }

    @DeleteMapping("/{stepId}/completion")
    @Operation(summary = "Undo today's completion of a step")
    public ResponseEntity<?> undo(@PathVariable Long profileId, @PathVariable Long stepId) {
        return toResponse(routineService.undo(profileId, stepId));
    }

    private ResponseEntity<?> toResponse(Result<RoutineStep, DomainError> result) {
        return result.fold(
                step -> ResponseEntity.ok(RoutineStepResource.from(step, routineService.today())),
                ProblemDetailResponseAssembler::toResponseEntity);
    }
}
