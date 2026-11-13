package com.acme.nubi.platform.profile.domain.repository;

import com.acme.nubi.platform.profile.domain.model.aggregates.Subscription;
import java.util.Optional;

/** Puerto de persistencia del agregado Subscription. */
public interface SubscriptionRepository {

    Subscription save(Subscription subscription);

    Optional<Subscription> findByAccountId(Long accountId);
}
