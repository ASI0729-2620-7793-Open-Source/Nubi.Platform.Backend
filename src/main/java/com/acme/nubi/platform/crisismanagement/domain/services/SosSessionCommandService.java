package com.acme.nubi.platform.crisismanagement.domain.services;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.CompleteStepCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.FinishSosSessionCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.RequestAlternativeCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.SkipStepCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.StartSosSessionCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.entities.AlternativeTechnique;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;

public interface SosSessionCommandService {
    Result<SosSession, DomainError> handle(StartSosSessionCommand command);

    Result<SosSession, DomainError> handle(CompleteStepCommand command);

    Result<SosSession, DomainError> handle(SkipStepCommand command);

    Result<AlternativeTechnique, DomainError> handle(RequestAlternativeCommand command);

    Result<SosSession, DomainError> handle(FinishSosSessionCommand command);
}
