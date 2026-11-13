package com.acme.nubi.platform.profile.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities.SubscriptionJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de la suscripción. */
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionJpaEntity, Long> {

    Optional<SubscriptionJpaEntity> findByAccountId(Long accountId);
}
