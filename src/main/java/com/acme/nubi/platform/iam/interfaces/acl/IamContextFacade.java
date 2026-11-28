package com.acme.nubi.platform.iam.interfaces.acl;

import com.acme.nubi.platform.iam.domain.model.aggregates.UserAccount;
import com.acme.nubi.platform.iam.domain.repositories.UserAccountRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fachada pública de IAM: los demás contextos solo reciben tipos primitivos. */
@Service
public class IamContextFacade {

    private final UserAccountRepository accounts;

    public IamContextFacade(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public Optional<String> fetchEmail(Long accountId) {
        return accounts.findById(accountId).map(UserAccount::getEmail);
    }
}
