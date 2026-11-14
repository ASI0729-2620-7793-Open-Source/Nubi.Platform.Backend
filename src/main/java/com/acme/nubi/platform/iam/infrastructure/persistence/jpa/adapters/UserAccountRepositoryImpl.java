package com.acme.nubi.platform.iam.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.iam.domain.model.aggregates.UserAccount;
import com.acme.nubi.platform.iam.domain.repositories.UserAccountRepository;
import com.acme.nubi.platform.iam.infrastructure.persistence.jpa.entities.UserAccountPersistenceEntity;
import com.acme.nubi.platform.iam.infrastructure.persistence.jpa.repositories.UserAccountPersistenceRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final UserAccountPersistenceRepository persistenceRepository;

    public UserAccountRepositoryImpl(UserAccountPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public UserAccount save(UserAccount account) {
        var entity = account.getId() == null
                ? new UserAccountPersistenceEntity()
                : persistenceRepository.findById(account.getId()).orElseGet(UserAccountPersistenceEntity::new);
        entity.setEmail(account.getEmail());
        entity.setFullName(account.getFullName());
        entity.setPasswordHash(account.getPasswordHash());
        return toDomain(persistenceRepository.save(entity));
    }

    @Override
    public Optional<UserAccount> findById(Long id) {
        return persistenceRepository.findById(id).map(UserAccountRepositoryImpl::toDomain);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return persistenceRepository.findByEmail(email).map(UserAccountRepositoryImpl::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return persistenceRepository.existsByEmail(email);
    }

    private static UserAccount toDomain(UserAccountPersistenceEntity entity) {
        return new UserAccount(entity.getId(), entity.getEmail(), entity.getFullName(),
                entity.getPasswordHash(), entity.getCreatedAt());
    }
}
