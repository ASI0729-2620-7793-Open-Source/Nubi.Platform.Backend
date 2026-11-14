package com.acme.nubi.platform.iam.domain.services;

import com.acme.nubi.platform.iam.domain.model.aggregates.UserAccount;
import com.acme.nubi.platform.iam.domain.model.commands.SignInCommand;
import com.acme.nubi.platform.iam.domain.model.commands.SignUpCommand;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import java.util.Optional;

public interface AuthenticationCommandService {

    Result<UserAccount, DomainError> handle(SignUpCommand command);

    Result<UserAccount, DomainError> handle(SignInCommand command);

    Optional<UserAccount> findById(Long accountId);
}
