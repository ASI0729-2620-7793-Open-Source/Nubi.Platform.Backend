package com.acme.nubi.platform.selfregulation.domain.services;

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
import com.acme.nubi.platform.selfregulation.domain.model.commands.StartCalmSessionCommand;
import com.acme.nubi.platform.selfregulation.domain.model.commands.StartCalmTimerCommand;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;

public interface CalmSessionCommandService {
    Result<CalmSession, DomainError> handle(StartCalmSessionCommand command);

    Result<CalmSession, DomainError> handle(SelectCalmingResourceCommand command);

    Result<CalmSession, DomainError> handle(AdjustIntensityCommand command);

    Result<CalmSession, DomainError> handle(ChangeLowStimulationCommand command);

    Result<CalmSession, DomainError> handle(PauseCalmSessionCommand command);

    Result<CalmSession, DomainError> handle(ResumeCalmSessionCommand command);

    Result<CalmSession, DomainError> handle(StartCalmTimerCommand command);

    Result<CalmSession, DomainError> handle(PauseCalmTimerCommand command);

    Result<CalmSession, DomainError> handle(ResumeCalmTimerCommand command);

    Result<CalmSession, DomainError> handle(AdvanceCalmTimerCommand command);

    Result<CalmSession, DomainError> handle(FinishCalmSessionCommand command);
}
