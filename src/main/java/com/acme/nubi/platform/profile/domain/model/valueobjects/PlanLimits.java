package com.acme.nubi.platform.profile.domain.model.valueobjects;

import com.acme.nubi.platform.profile.domain.errors.DomainRuleViolation;
import com.acme.nubi.platform.profile.domain.model.enums.PlanType;

/**
 * Value Object. Límites que un plan impone sobre los perfiles y los
 * cuidadores por perfil. Los valores se definieron en el diseño del producto:
 * FREEMIUM 3/2, FAMILY_PREMIUM 10/5, INSTITUTIONAL 200/50.
 */
public record PlanLimits(int maxProfiles, int maxCaregiversPerProfile) {

    public PlanLimits {
        if (maxProfiles <= 0 || maxCaregiversPerProfile <= 0) {
            throw new DomainRuleViolation(
                    "plan.limits.invalid",
                    "Los límites del plan deben ser mayores que cero.",
                    "limits");
        }
    }

    /** Límites del plan, usados al asignar o actualizar una suscripción. */
    public static PlanLimits of(PlanType plan) {
        return switch (plan) {
            case FREEMIUM -> new PlanLimits(3, 2);
            case FAMILY_PREMIUM -> new PlanLimits(10, 5);
            case INSTITUTIONAL -> new PlanLimits(200, 50);
        };
    }

    public boolean allowsAnotherProfile(int currentProfiles) {
        return currentProfiles < maxProfiles;
    }

    public boolean allowsAnotherCaregiver(int currentCaregivers) {
        return currentCaregivers < maxCaregiversPerProfile;
    }
}
