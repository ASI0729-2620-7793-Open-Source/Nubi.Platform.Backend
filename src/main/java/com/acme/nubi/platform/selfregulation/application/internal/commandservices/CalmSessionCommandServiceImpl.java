package com.acme.nubi.platform.selfregulation.application.internal.commandservices;

import com.acme.nubi.platform.selfregulation.application.internal.outboundservices.acl.ExternalProfileService;
import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmSession;
import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmingResource;
import com.acme.nubi.platform.selfregulation.domain.model.commands.AdjustIntensityCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.AdvanceCalmTimerCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.ChangeLowStimulationCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.FinishCalmSessionCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.PauseCalmSessionCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.PauseCalmTimerCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.ResumeCalmSessionCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.ResumeCalmTimerCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.SelectCalmingResourceCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.StartCalmSessionCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.StartCalmTimerCommand;
import com.acme.nubi.platform.selfregulation.domain.model.errors.SelfRegulationErrors;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.domain.repositories.CalmSessionRepository;
import com.acme.nubi.platform.selfregulation.domain.repositories.CalmingResourceRepository;
import com.acme.nubi.platform.selfregulation.domain.services.CalmSessionCommandService;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Consumer;

@Service
@Transactional
public class CalmSessionCommandServiceImpl implements CalmSessionCommandService {

    private final CalmSessionRepository calmSessionRepository;
    private final CalmingResourceRepository calmingResourceRepository;
    private final ExternalProfileService externalProfileService;
    private final Clock clock;

    public CalmSessionCommandServiceImpl(CalmSessionRepository calmSessionRepository,
                                         CalmingResourceRepository calmingResourceRepository,
                                         ExternalProfileService externalProfileService,
                                         Clock clock) {
        this.calmSessionRepository = calmSessionRepository;
        this.calmingResourceRepository = calmingResourceRepository;
        this.externalProfileService = externalProfileService;
        this.clock = clock;
    }

    @Override
    public Result<CalmSession, DomainError> handle(StartCalmSessionCommand command) {
        var profileId = new ProfileId(command.profileId());
        if (!externalProfileService.existsProfile(profileId)) {
            return Result.failure(SelfRegulationErrors.profileNotFound(command.profileId()));
        }

        CalmingResource resource = null;
        if (command.resourceId() != null) {
            var foundResource = calmingResourceRepository.findById(command.resourceId());
            if (foundResource.isEmpty()) {
                return Result.failure(SelfRegulationErrors.calmingResourceNotFound(command.resourceId()));
            }
            resource = foundResource.get();
        }

        try {
            var session = new CalmSession(profileId, resource, command.startedBySos(), Instant.now(clock));
            return Result.success(calmSessionRepository.save(session));
        } catch (IllegalStateException exception) {
            return Result.failure(SelfRegulationErrors.invalidCalmSessionOperation(exception.getMessage()));
        }
    }

    @Override
    public Result<CalmSession, DomainError> handle(SelectCalmingResourceCommand command) {
        var resource = calmingResourceRepository.findById(command.resourceId());
        if (resource.isEmpty()) {
            return Result.failure(SelfRegulationErrors.calmingResourceNotFound(command.resourceId()));
        }
        return update(command.calmSessionId(), session -> session.selectResource(resource.get()));
    }

    @Override
    public Result<CalmSession, DomainError> handle(AdjustIntensityCommand command) {
        var session = calmSessionRepository.findById(command.calmSessionId());
        if (session.isEmpty()) {
            return Result.failure(SelfRegulationErrors.calmSessionNotFound(command.calmSessionId()));
        }
        var resourceId = session.get().getResourceId();
        if (resourceId == null) {
            return Result.failure(SelfRegulationErrors.invalidCalmSessionOperation(
                    "Select a calming resource before adjusting the intensity."));
        }
        var resource = calmingResourceRepository.findById(resourceId);
        if (resource.isEmpty()) {
            return Result.failure(SelfRegulationErrors.calmingResourceNotFound(resourceId));
        }
        return apply(session.get(), current -> current.adjustIntensity(command.intensity(), resource.get()));
    }

    @Override
    public Result<CalmSession, DomainError> handle(ChangeLowStimulationCommand command) {
        return update(command.calmSessionId(), session -> {
            if (command.active()) {
                session.activateLowStimulation();
            } else {
                session.deactivateLowStimulation();
            }
        });
    }

    @Override
    public Result<CalmSession, DomainError> handle(PauseCalmSessionCommand command) {
        return update(command.calmSessionId(), CalmSession::pause);
    }

    @Override
    public Result<CalmSession, DomainError> handle(ResumeCalmSessionCommand command) {
        return update(command.calmSessionId(), CalmSession::resume);
    }

    @Override
    public Result<CalmSession, DomainError> handle(StartCalmTimerCommand command) {
        return update(command.calmSessionId(), session -> session.startTimer(command.durationMinutes()));
    }

    @Override
    public Result<CalmSession, DomainError> handle(PauseCalmTimerCommand command) {
        return update(command.calmSessionId(), CalmSession::pauseTimer);
    }

    @Override
    public Result<CalmSession, DomainError> handle(ResumeCalmTimerCommand command) {
        return update(command.calmSessionId(), CalmSession::resumeTimer);
    }

    @Override
    public Result<CalmSession, DomainError> handle(AdvanceCalmTimerCommand command) {
        return update(command.calmSessionId(), session -> session.advanceTimer(command.elapsedSeconds()));
    }

    @Override
    public Result<CalmSession, DomainError> handle(FinishCalmSessionCommand command) {
        return update(command.calmSessionId(), session -> session.finish(Instant.now(clock)));
    }

    private Result<CalmSession, DomainError> update(Long calmSessionId, Consumer<CalmSession> action) {
        return calmSessionRepository.findById(calmSessionId)
                .map(session -> apply(session, action))
                .orElseGet(() -> Result.failure(SelfRegulationErrors.calmSessionNotFound(calmSessionId)));
    }

    /** Ejecuta la regla del agregado y guarda; si el agregado la rechaza, devuelve el error. */
    private Result<CalmSession, DomainError> apply(CalmSession session, Consumer<CalmSession> action) {
        try {
            action.accept(session);
        } catch (IllegalStateException exception) {
            return Result.failure(SelfRegulationErrors.invalidCalmSessionOperation(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return Result.failure(SelfRegulationErrors.invalidCalmSessionData(exception.getMessage()));
        }
        return Result.success(calmSessionRepository.save(session));
    }
}
