package com.acme.nubi.platform.profile.domain.model.valueobjects;

import com.acme.nubi.platform.profile.domain.errors.DomainRuleViolation;
import com.acme.nubi.platform.profile.domain.model.enums.PlanType;

/**
 * Value Object. Límites que un plan impone sobre los perfiles y los cuidadores por perfil.
 *
 * <p>Cada cuenta acompaña a un único niño con autismo, sea cual sea el plan: lo que cambia
 * entre planes es cuántos cuidadores (familia, terapeutas, docentes) pueden sumarse.
 * FREEMIUM 1/2, FAMILY_PREMIUM 1/5, INSTITUTIONAL 1/50.
 */
public record PlanLimits(int maxProfiles, int maxCaregiversPerProfile) {

    /** Una cuenta, un niño. */
    public static final int SINGLE_CHILD = 1;

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
            case FREEMIUM -> new PlanLimits(SINGLE_CHILD, 2);
            case FAMILY_PREMIUM -> new PlanLimits(SINGLE_CHILD, 5);
            case INSTITUTIONAL -> new PlanLimits(SINGLE_CHILD, 50);
        };
    }

    public boolean allowsAnotherProfile(int currentProfiles) {
        return currentProfiles < maxProfiles;
    }

    public boolean allowsAnotherCaregiver(int currentCaregivers) {
        return currentCaregivers < maxCaregiversPerProfile;
    }
}
