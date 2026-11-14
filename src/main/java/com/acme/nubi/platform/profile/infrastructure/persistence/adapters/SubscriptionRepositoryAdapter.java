package com.acme.nubi.platform.profile.infrastructure.persistence.adapters;

import com.acme.nubi.platform.profile.domain.model.aggregates.Subscription;
import com.acme.nubi.platform.profile.domain.repository.SubscriptionRepository;
import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities.SubscriptionJpaEntity;
import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.repositories.SubscriptionJpaRepository;
import com.acme.nubi.platform.profile.infrastructure.persistence.mappers.SubscriptionMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador de persistencia del agregado Subscription. */
@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SubscriptionJpaRepository jpaRepository;

    @Override
    @Transactional
    public Subscription save(Subscription subscription) {
        SubscriptionJpaEntity entity = subscription.id() == null
                ? jpaRepository.findByAccountId(subscription.accountId())
                        .orElseGet(SubscriptionJpaEntity::new)
                : jpaRepository.findById(subscription.id())
                        .orElseThrow(() -> new IllegalStateException(
                                "Suscripción " + subscription.id() + " ya no existe."));
        SubscriptionMapper.applyTo(entity, subscription);
        return SubscriptionMapper.toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findByAccountId(Long accountId) {
        return jpaRepository.findByAccountId(accountId).map(SubscriptionMapper::toDomain);
    }
}
