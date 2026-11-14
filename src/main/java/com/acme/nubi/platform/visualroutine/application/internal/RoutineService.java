package com.acme.nubi.platform.visualroutine.application.internal;

import com.acme.nubi.platform.profile.interfaces.acl.ProfilesContextFacade;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import com.acme.nubi.platform.visualroutine.domain.model.aggregates.RoutineStep;
import com.acme.nubi.platform.visualroutine.domain.model.valueobjects.DefaultRoutine;
import com.acme.nubi.platform.visualroutine.domain.repositories.RoutineStepRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de la Rutina Visual: consultar el día (con la rutina sugerida si aún no hay
 * pasos), agregar, editar, reordenar, quitar y marcar pasos como hechos hoy.
 */
@Service
@Transactional
public class RoutineService {

    public static final int MAX_STEPS = 20;

    private final RoutineStepRepository routineStepRepository;
    private final ProfilesContextFacade profilesContextFacade;
    private final Clock clock;

    public RoutineService(RoutineStepRepository routineStepRepository,
                          ProfilesContextFacade profilesContextFacade,
                          Clock clock) {
        this.routineStepRepository = routineStepRepository;
        this.profilesContextFacade = profilesContextFacade;
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public Result<List<RoutineStep>, DomainError> getRoutine(Long profileId) {
        if (!profilesContextFacade.existsActiveProfile(profileId)) {
            return Result.failure(profileNotFound(profileId));
        }
        var steps = routineStepRepository.findByProfileId(profileId);
        if (steps.isEmpty()) {
            var provisioned = new ArrayList<RoutineStep>();
            var position = 0;
            for (var template : DefaultRoutine.STEPS) {
                provisioned.add(routineStepRepository.save(
                        RoutineStep.create(profileId, template.title(), template.icon(), template.time(), position++)));
            }
            return Result.success(provisioned);
        }
        return Result.success(steps);
    }

    public Result<RoutineStep, DomainError> addStep(Long profileId, String title, String icon, LocalTime time) {
        var current = getRoutine(profileId);
        if (current.isFailure()) {
            return Result.failure(current.failure().orElseThrow());
        }
        var steps = current.success().orElseThrow();
        if (steps.size() >= MAX_STEPS) {
            return Result.failure(DomainError.conflict("routine.steps.limit",
                    "La rutina admite hasta " + MAX_STEPS + " pasos para no saturar al niño."));
        }
        var position = steps.stream().mapToInt(RoutineStep::getPosition).max().orElse(-1) + 1;
        try {
            return Result.success(routineStepRepository.save(RoutineStep.create(profileId, title, icon, time, position)));
        } catch (IllegalArgumentException exception) {
            return Result.failure(DomainError.validation("routine.step.invalid", exception.getMessage()));
        }
    }

    public Result<RoutineStep, DomainError> updateStep(Long profileId, Long stepId, String title, String icon,
                                                       LocalTime time) {
        return withStep(profileId, stepId, step -> {
            step.rename(title, icon, time);
            return step;
        });
    }

    public Result<RoutineStep, DomainError> complete(Long profileId, Long stepId) {
        return withStep(profileId, stepId, step -> {
            step.completeOn(today());
            return step;
        });
    }

    public Result<RoutineStep, DomainError> undo(Long profileId, Long stepId) {
        return withStep(profileId, stepId, step -> {
            step.undoCompletion(today());
            return step;
        });
    }

    public Result<Boolean, DomainError> remove(Long profileId, Long stepId) {
        var step = findOwned(profileId, stepId);
        if (step.isEmpty()) {
            return Result.failure(stepNotFound(stepId));
        }
        routineStepRepository.delete(step.get());
        return Result.success(true);
    }

    public Result<List<RoutineStep>, DomainError> reorder(Long profileId, List<Long> order) {
        var current = getRoutine(profileId);
        if (current.isFailure()) {
            return current;
        }
        var steps = current.success().orElseThrow();
        if (order == null || order.size() != steps.size()
                || !steps.stream().map(RoutineStep::getId).allMatch(order::contains)) {
            return Result.failure(DomainError.validation("routine.order.invalid",
                    "El nuevo orden debe incluir todos los pasos de la rutina una sola vez."));
        }
        for (var step : steps) {
            step.moveTo(order.indexOf(step.getId()));
            routineStepRepository.save(step);
        }
        return Result.success(routineStepRepository.findByProfileId(profileId));
    }

    private Result<RoutineStep, DomainError> withStep(Long profileId, Long stepId,
                                                      java.util.function.UnaryOperator<RoutineStep> change) {
        var step = findOwned(profileId, stepId);
        if (step.isEmpty()) {
            return Result.failure(stepNotFound(stepId));
        }
        try {
            return Result.success(routineStepRepository.save(change.apply(step.get())));
        } catch (IllegalArgumentException exception) {
            return Result.failure(DomainError.validation("routine.step.invalid", exception.getMessage()));
        }
    }

    private Optional<RoutineStep> findOwned(Long profileId, Long stepId) {
        return routineStepRepository.findById(stepId).filter(step -> step.getProfileId().equals(profileId));
    }

    private static DomainError profileNotFound(Long profileId) {
        return DomainError.notFound("routine.profile.notFound", "No existe el perfil " + profileId + ".");
    }

    private static DomainError stepNotFound(Long stepId) {
        return DomainError.notFound("routine.step.notFound", "El paso " + stepId + " no es parte de esta rutina.");
    }
}
