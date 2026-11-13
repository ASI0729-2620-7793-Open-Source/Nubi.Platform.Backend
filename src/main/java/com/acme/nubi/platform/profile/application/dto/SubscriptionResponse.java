package com.acme.nubi.platform.profile.application.dto;

import com.acme.nubi.platform.profile.domain.model.aggregates.Subscription;
import com.acme.nubi.platform.profile.domain.model.enums.PlanType;
import com.acme.nubi.platform.profile.domain.model.enums.SubscriptionStatus;
import java.time.Instant;

/** Contrato de lectura de la suscripción y sus límites de plan. */
public record SubscriptionResponse(
        Long id,
        Long accountId,
        PlanType plan,
        SubscriptionStatus status,
        int maxProfiles,
        int maxCaregiversPerProfile,
        Instant startedAt,
        Instant endsAt) {

    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.id(),
                subscription.accountId(),
                subscription.plan(),
                subscription.status(),
                subscription.limits().maxProfiles(),
                subscription.limits().maxCaregiversPerProfile(),
                subscription.startedAt(),
                subscription.endsAt());
    }
}
