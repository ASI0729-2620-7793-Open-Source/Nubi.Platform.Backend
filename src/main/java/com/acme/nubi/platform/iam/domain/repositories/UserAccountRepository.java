package com.acme.nubi.platform.iam.domain.repositories;

import com.acme.nubi.platform.iam.domain.model.aggregates.UserAccount;
import java.util.Optional;

public interface UserAccountRepository {

    UserAccount save(UserAccount account);

    Optional<UserAccount> findById(Long id);

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
