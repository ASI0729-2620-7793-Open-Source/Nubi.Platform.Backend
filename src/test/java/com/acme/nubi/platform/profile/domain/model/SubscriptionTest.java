package com.acme.nubi.platform.profile.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acme.nubi.platform.profile.domain.model.aggregates.Subscription;
import com.acme.nubi.platform.profile.domain.model.enums.PlanType;
import com.acme.nubi.platform.profile.domain.model.enums.SubscriptionStatus;
import com.acme.nubi.platform.profile.domain.model.valueobjects.PlanLimits;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Límites de plan y estado de la suscripción (US-46 a US-48). */
class SubscriptionTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    @DisplayName("Toda cuenta nueva nace con el plan gratuito y suscripción activa")
    void everyAccountStartsFreemiumAndActive() {
        Subscription subscription = Subscription.freemium(42L, NOW);

        assertEquals(Long.valueOf(42L), subscription.accountId());
        assertEquals(PlanType.FREEMIUM, subscription.plan());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.status());
        assertEquals(3, subscription.limits().maxProfiles());
        assertEquals(2, subscription.limits().maxCaregiversPerProfile());
    }

    @Test
    @DisplayName("Cada plan impone sus límites de perfiles y cuidadores")
    void planLimits() {
        PlanLimits freemium = PlanLimits.of(PlanType.FREEMIUM);
        assertEquals(3, freemium.maxProfiles());
        assertEquals(2, freemium.maxCaregiversPerProfile());

        PlanLimits family = PlanLimits.of(PlanType.FAMILY_PREMIUM);
        assertEquals(10, family.maxProfiles());
        assertEquals(5, family.maxCaregiversPerProfile());

        PlanLimits institutional = PlanLimits.of(PlanType.INSTITUTIONAL);
        assertEquals(200, institutional.maxProfiles());
        assertEquals(50, institutional.maxCaregiversPerProfile());
    }

    @Test
    @DisplayName("El cupo se agota justo en el último permitido, no antes ni después")
    void limitIsExact() {
        PlanLimits freemium = PlanLimits.of(PlanType.FREEMIUM);

        assertTrue(freemium.allowsAnotherProfile(0));
        assertTrue(freemium.allowsAnotherProfile(2));
        assertFalse(freemium.allowsAnotherProfile(3));

        assertTrue(freemium.allowsAnotherCaregiver(1));
        assertFalse(freemium.allowsAnotherCaregiver(2));
    }

    @Test
    @DisplayName("Cambiar de plan recalcula los límites al instante (US-48)")
    void changingPlanRecalculatesLimits() {
        Subscription upgraded = Subscription.freemium(42L, NOW)
                .changePlan(PlanType.FAMILY_PREMIUM);

        assertEquals(PlanType.FAMILY_PREMIUM, upgraded.plan());
        assertEquals(SubscriptionStatus.ACTIVE, upgraded.status());
        assertEquals(10, upgraded.limits().maxProfiles());
        assertEquals(5, upgraded.limits().maxCaregiversPerProfile());
        assertTrue(upgraded.allowsAnotherProfile(9));
        assertFalse(upgraded.allowsAnotherProfile(10));
    }

    @Test
    @DisplayName("Una suscripción cancelada no permite crear más perfiles ni cuidadores")
    void cancelledSubscriptionBlocksNewResources() {
        Subscription cancelled = Subscription.freemium(42L, NOW).cancel();

        assertEquals(SubscriptionStatus.CANCELLED, cancelled.status());
        assertFalse(cancelled.allowsAnotherProfile(0));
        assertFalse(cancelled.allowsAnotherCaregiver(0));
    }

    @Test
    @DisplayName("La suscripción conserva el id y la cuenta al cambiar de plan")
    void identitySurvivesPlanChange() {
        Subscription original = new Subscription(
                7L, 42L, PlanType.FREEMIUM, SubscriptionStatus.ACTIVE,
                PlanLimits.of(PlanType.FREEMIUM), NOW, null, NOW, NOW);

        Subscription changed = original.changePlan(PlanType.INSTITUTIONAL);

        assertEquals(Long.valueOf(7L), changed.id());
        assertEquals(Long.valueOf(42L), changed.accountId());
        assertEquals(200, changed.limits().maxProfiles());
    }
}
