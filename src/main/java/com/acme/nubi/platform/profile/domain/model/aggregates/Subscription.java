package com.acme.nubi.platform.profile.domain.model.aggregates;

import com.acme.nubi.platform.profile.domain.errors.DomainRuleViolation;
import com.acme.nubi.platform.profile.domain.model.enums.PlanType;
import com.acme.nubi.platform.profile.domain.model.enums.SubscriptionStatus;
import com.acme.nubi.platform.profile.domain.model.valueobjects.PlanLimits;
import com.acme.nubi.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.time.Instant;

/**
 * Aggregate Root. Suscripción de una cuenta dentro de este Bounded Context.
 *
 * <p>Una cuenta tiene como máximo una suscripción vigente. El plan define los
 * límites de perfiles y de cuidadores por perfil (US-06, Scenario 2).
 */
public class Subscription extends AbstractDomainAggregateRoot {

    private final Long id;
    private final Long accountId;
    private final PlanType plan;
    private final SubscriptionStatus status;
    private final PlanLimits limits;
    private final Instant startedAt;
    private final Instant endsAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Subscription(
            Long id,
            Long accountId,
            PlanType plan,
            SubscriptionStatus status,
            PlanLimits limits,
            Instant startedAt,
            Instant endsAt,
            Instant createdAt,
            Instant updatedAt) {
        if (accountId == null) {
            throw new DomainRuleViolation(
                    "subscription.account.required", "La suscripción necesita una cuenta.",
                    "accountId");
        }
        if (plan == null) {
            throw new DomainRuleViolation(
                    "subscription.plan.required", "El plan es obligatorio.", "plan");
        }
        this.id = id;
        this.accountId = accountId;
        this.plan = plan;
        this.status = status;
        this.limits = limits;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Toda cuenta nueva nace en el plan gratuito con sus límites (US-46). */
    public static Subscription freemium(Long accountId, Instant now) {
        return new Subscription(
                null,
                accountId,
                PlanType.FREEMIUM,
                SubscriptionStatus.ACTIVE,
                PlanLimits.of(PlanType.FREEMIUM),
                now,
                null,
                now,
                now);
    }

    /** Cambio de plan: recalcula los límites de forma atómica. */
    public Subscription changePlan(PlanType newPlan) {
        if (newPlan == null) {
            throw new DomainRuleViolation(
                    "subscription.plan.required", "El plan es obligatorio.", "plan");
        }
        return new Subscription(
                id,
                accountId,
                newPlan,
                SubscriptionStatus.ACTIVE,
                PlanLimits.of(newPlan),
                Instant.now(),
                null,
                createdAt,
                Instant.now());
    }

    public Subscription cancel() {
        return new Subscription(
                id, accountId, plan, SubscriptionStatus.CANCELLED, limits, startedAt, endsAt,
                createdAt, Instant.now());
    }

    public boolean allowsAnotherProfile(int currentProfiles) {
        return status == SubscriptionStatus.ACTIVE && limits.allowsAnotherProfile(currentProfiles);
    }

    public boolean allowsAnotherCaregiver(int currentCaregivers) {
        return status == SubscriptionStatus.ACTIVE
                && limits.allowsAnotherCaregiver(currentCaregivers);
    }

    public boolean allowsProfileCount(int currentProfiles) {
        return status == SubscriptionStatus.ACTIVE
                && limits.allowsAnotherProfile(currentProfiles - 1);
    }

    public Long id() {
        return id;
    }

    public Long accountId() {
        return accountId;
    }

    public PlanType plan() {
        return plan;
    }

    public SubscriptionStatus status() {
        return status;
    }

    public PlanLimits limits() {
        return limits;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant endsAt() {
        return endsAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
