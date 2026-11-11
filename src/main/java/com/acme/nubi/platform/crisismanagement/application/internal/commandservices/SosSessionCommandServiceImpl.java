package com.acme.nubi.platform.crisismanagement.application.internal.commandservices;

import com.acme.nubi.platform.crisismanagement.application.internal.outboundservices.acl.ExternalProfileService;
import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.CompleteStepCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.FinishSosSessionCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.RequestAlternativeCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.SkipStepCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.StartSosSessionCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.AlternativeTechnique;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.ContainmentStep;
import com.acme.nubi.platform.crisismanagement.domain.model.errors.CrisisManagementErrors;
import com.acme.nubi.platform.crisismanagement.domain.model.events.EpisodeFinishedEvent;
import com.acme.nubi.platform.crisismanagement.domain.model.events.SosModeActivatedEvent;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.AccountId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryProfileSnapshot;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.crisismanagement.domain.repositories.ActionGuideRepository;
import com.acme.nubi.platform.crisismanagement.domain.repositories.SosSessionRepository;
import com.acme.nubi.platform.crisismanagement.domain.services.SosSessionCommandService;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@Service
@Transactional
public class SosSessionCommandServiceImpl implements SosSessionCommandService {

    private final SosSessionRepository sosSessionRepository;
    private final ActionGuideRepository actionGuideRepository;
    private final ExternalProfileService externalProfileService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public SosSessionCommandServiceImpl(SosSessionRepository sosSessionRepository,
                                        ActionGuideRepository actionGuideRepository,
                                        ExternalProfileService externalProfileService,
                                        ApplicationEventPublisher eventPublisher,
                                        Clock clock) {
        this.sosSessionRepository = sosSessionRepository;
        this.actionGuideRepository = actionGuideRepository;
        this.externalProfileService = externalProfileService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    public Result<SosSession, DomainError> handle(StartSosSessionCommand command) {
        var profileId = new ProfileId(command.profileId());
        if (!externalProfileService.existsProfile(profileId)) {
            return Result.failure(CrisisManagementErrors.profileNotFound(command.profileId()));
        }
        // Solo puede haber una sesión en curso por perfil. MySQL no admite un índice único parcial,
        // por eso la regla se aplica aquí.
        if (sosSessionRepository.findActiveByProfileId(profileId).isPresent()) {
            return Result.failure(CrisisManagementErrors.activeSosSessionAlreadyExists(command.profileId()));
        }
        var guide = actionGuideRepository.findActiveGuide();
        if (guide.isEmpty()) {
            return Result.failure(CrisisManagementErrors.activeActionGuideNotFound());
        }

        // Si el cuidador no indica detonante, se usa el sentido más sensible del perfil sensorial.
        var trigger = command.trigger() != null
                ? command.trigger()
                : externalProfileService.fetchSensoryProfile(profileId)
                .map(SensoryProfileSnapshot::mostSensitiveTrigger)
                .orElse(SensoryTrigger.NONE);

        try {
            var session = SosSession.start(profileId, new AccountId(command.caregiverAccountId()), guide.get(),
                    trigger, command.initialIntensity(), Instant.now(clock));
            var saved = sosSessionRepository.save(session);
            eventPublisher.publishEvent(SosModeActivatedEvent.from(saved));
            return Result.success(saved);
        } catch (IllegalStateException exception) {
            return Result.failure(CrisisManagementErrors.invalidSosSessionOperation(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return Result.failure(CrisisManagementErrors.invalidSosSessionData(exception.getMessage()));
        }
    }

    @Override
    public Result<SosSession, DomainError> handle(CompleteStepCommand command) {
        return handleStep(command.sosSessionId(), command.stepId(),
                (session, context) -> session.completeStep(context.step(), context.orderedSteps(), Instant.now(clock)));
    }

    @Override
    public Result<SosSession, DomainError> handle(SkipStepCommand command) {
        return handleStep(command.sosSessionId(), command.stepId(),
                (session, context) -> session.skipStep(context.step(), context.orderedSteps(),
                        command.confirmed(), Instant.now(clock)));
    }

    @Override
    public Result<AlternativeTechnique, DomainError> handle(RequestAlternativeCommand command) {
        var session = sosSessionRepository.findById(command.sosSessionId());
        if (session.isEmpty()) {
            return Result.failure(CrisisManagementErrors.sosSessionNotFound(command.sosSessionId()));
        }
        if (!session.get().isOpen()) {
            return Result.failure(CrisisManagementErrors.invalidSosSessionOperation("The SOS session is already finished."));
        }
        var context = resolveStep(session.get(), command.stepId());
        if (context.isEmpty()) {
            return Result.failure(CrisisManagementErrors.stepNotFound(command.sosSessionId(), command.stepId()));
        }
        return context.get().step().findAlternativeTechnique()
                .<Result<AlternativeTechnique, DomainError>>map(Result::success)
                .orElseGet(() -> Result.failure(CrisisManagementErrors.alternativeTechniqueNotFound(command.stepId())));
    }

    @Override
    public Result<SosSession, DomainError> handle(FinishSosSessionCommand command) {
        var found = sosSessionRepository.findById(command.sosSessionId());
        if (found.isEmpty()) {
            return Result.failure(CrisisManagementErrors.sosSessionNotFound(command.sosSessionId()));
        }
        var session = found.get();
        try {
            var now = Instant.now(clock);
            if (session.allStepsHandled()) {
                session.finish(command.observedState(), command.finalIntensity(), now);
            } else {
                session.finishEarly(command.observedState(), command.finalIntensity(), now);
            }
        } catch (IllegalStateException exception) {
            return Result.failure(CrisisManagementErrors.invalidSosSessionOperation(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return Result.failure(CrisisManagementErrors.invalidSosSessionData(exception.getMessage()));
        }
        var saved = sosSessionRepository.save(session);
        eventPublisher.publishEvent(EpisodeFinishedEvent.from(saved));
        return Result.success(saved);
    }

    private Result<SosSession, DomainError> handleStep(Long sosSessionId, Long stepId,
                                                       BiConsumer<SosSession, StepContext> action) {
        var found = sosSessionRepository.findById(sosSessionId);
        if (found.isEmpty()) {
            return Result.failure(CrisisManagementErrors.sosSessionNotFound(sosSessionId));
        }
        var session = found.get();
        var context = resolveStep(session, stepId);
        if (context.isEmpty()) {
            return Result.failure(CrisisManagementErrors.stepNotFound(sosSessionId, stepId));
        }
        try {
            action.accept(session, context.get());
        } catch (IllegalStateException exception) {
            return Result.failure(CrisisManagementErrors.invalidSosSessionOperation(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return Result.failure(CrisisManagementErrors.invalidSosSessionData(exception.getMessage()));
        }
        return Result.success(sosSessionRepository.save(session));
    }

    /** Paso pedido más los pasos de la guía en el orden personalizado de la sesión. */
    private Optional<StepContext> resolveStep(SosSession session, Long stepId) {
        return actionGuideRepository.findById(session.getGuideId())
                .flatMap(guide -> guide.findStepById(stepId)
                        .map(step -> new StepContext(step, guide.orderedStepsFor(session.getTrigger()))));
    }

    private record StepContext(ContainmentStep step, List<ContainmentStep> orderedSteps) {
    }
}
