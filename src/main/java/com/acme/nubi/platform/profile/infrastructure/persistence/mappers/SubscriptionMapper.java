package com.acme.nubi.platform.profile.infrastructure.persistence.mappers;

import com.acme.nubi.platform.profile.domain.model.aggregates.Subscription;
import com.acme.nubi.platform.profile.domain.model.valueobjects.PlanLimits;
import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities.SubscriptionJpaEntity;

/** Mapeo explícito entre el agregado Subscription y su entidad JPA. */
public final class SubscriptionMapper {

    private SubscriptionMapper() {
    }

    public static Subscription toDomain(SubscriptionJpaEntity entity) {
        return new Subscription(
                entity.getId(),
                entity.getAccountId(),
                entity.getPlan(),
                entity.getStatus(),
                PlanLimits.of(entity.getPlan()),
                entity.getStartedAt(),
                entity.getEndsAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static void applyTo(SubscriptionJpaEntity target, Subscription source) {
        target.setAccountId(source.accountId());
        target.setPlan(source.plan());
        target.setStatus(source.status());
        target.setStartedAt(source.startedAt());
        target.setEndsAt(source.endsAt());
    }
}
