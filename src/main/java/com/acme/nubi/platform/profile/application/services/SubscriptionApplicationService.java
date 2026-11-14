package com.acme.nubi.platform.profile.application.services;

import com.acme.nubi.platform.profile.application.dto.ProfileRequests.ChangePlanRequest;
import com.acme.nubi.platform.profile.application.dto.SubscriptionResponse;
import com.acme.nubi.platform.profile.application.errors.BusinessError;
import com.acme.nubi.platform.profile.application.errors.DomainGuard;
import com.acme.nubi.platform.profile.domain.model.aggregates.Subscription;
import com.acme.nubi.platform.profile.domain.repository.SubscriptionRepository;
import com.acme.nubi.platform.shared.application.result.Result;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de la suscripción y los límites de plan (US-46 a US-48).
 * Toda cuenta sin suscripción nace en FREEMIUM.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionApplicationService {

    private final SubscriptionRepository subscriptionRepository;

    /** Estado actual de la suscripción; crea la de forma implícita si falta. */
    @Transactional
    public Result<SubscriptionResponse, BusinessError> current(Long accountId) {
        return DomainGuard.map(currentSubscription(accountId), SubscriptionResponse::from);
    }

    @Transactional
    public Result<SubscriptionResponse, BusinessError> changePlan(
            Long accountId, ChangePlanRequest request) {
        Result<Subscription, BusinessError> current = currentSubscription(accountId);
        if (current.isFailure()) {
            return Result.failure(current.failure().orElseThrow());
        }
        Subscription subscription = current.success().orElseThrow();
        return DomainGuard.map(
                DomainGuard.guard(() ->
                        subscriptionRepository.save(subscription.changePlan(request.plan()))),
                SubscriptionResponse::from);
    }

    private Result<Subscription, BusinessError> currentSubscription(Long accountId) {
        Optional<Subscription> existing = subscriptionRepository.findByAccountId(accountId);
        if (existing.isPresent()) {
            return Result.success(existing.get());
        }
        return DomainGuard.guard(() ->
                subscriptionRepository.save(Subscription.freemium(accountId, Instant.now())));
    }
}
