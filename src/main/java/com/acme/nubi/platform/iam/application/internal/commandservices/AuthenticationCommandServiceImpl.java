package com.acme.nubi.platform.iam.application.internal.commandservices;

import com.acme.nubi.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.acme.nubi.platform.iam.domain.model.aggregates.UserAccount;
import com.acme.nubi.platform.iam.domain.model.commands.SignInCommand;
import com.acme.nubi.platform.iam.domain.model.commands.SignUpCommand;
import com.acme.nubi.platform.iam.domain.model.errors.IamErrors;
import com.acme.nubi.platform.iam.domain.repositories.UserAccountRepository;
import com.acme.nubi.platform.iam.domain.services.AuthenticationCommandService;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthenticationCommandServiceImpl implements AuthenticationCommandService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserAccountRepository userAccountRepository;
    private final HashingService hashingService;
    private final Clock clock;

    public AuthenticationCommandServiceImpl(UserAccountRepository userAccountRepository,
                                            HashingService hashingService,
                                            Clock clock) {
        this.userAccountRepository = userAccountRepository;
        this.hashingService = hashingService;
        this.clock = clock;
    }

    @Override
    public Result<UserAccount, DomainError> handle(SignUpCommand command) {
        if (command.password() == null || command.password().length() < MIN_PASSWORD_LENGTH) {
            return Result.failure(IamErrors.weakPassword());
        }
        var email = UserAccount.normalizeEmail(command.email());
        if (userAccountRepository.existsByEmail(email)) {
            return Result.failure(IamErrors.emailAlreadyRegistered());
        }
        try {
            var account = UserAccount.register(
                    email, command.fullName(), hashingService.encode(command.password()), Instant.now(clock));
            return Result.success(userAccountRepository.save(account));
        } catch (IllegalArgumentException exception) {
            return Result.failure(IamErrors.invalidData(exception.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<UserAccount, DomainError> handle(SignInCommand command) {
        return userAccountRepository.findByEmail(UserAccount.normalizeEmail(command.email()))
                .filter(account -> hashingService.matches(command.password(), account.getPasswordHash()))
                .<Result<UserAccount, DomainError>>map(Result::success)
                .orElseGet(() -> Result.failure(IamErrors.invalidCredentials()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findById(Long accountId) {
        return userAccountRepository.findById(accountId);
    }
}
