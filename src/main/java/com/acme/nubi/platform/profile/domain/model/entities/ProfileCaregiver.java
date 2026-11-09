package com.acme.nubi.platform.profile.domain.model.entities;

import com.acme.nubi.platform.profile.domain.errors.DomainRuleViolation;
import com.acme.nubi.platform.profile.domain.model.enums.CaregiverRole;
import com.acme.nubi.platform.profile.domain.model.enums.InvitationStatus;
import java.time.Instant;

/**
 * Entidad. Cuidador asociado a un perfil (US-06).
 * Nace PENDING con solo el correo y accountId vacío; al aceptar pasa a ACTIVE.
 * El id es null mientras la entidad no se persiste (lo asigna la base de datos).
 */
public class ProfileCaregiver {

    private final Long id;
    private final Long accountId;
    private final String invitedEmail;
    private final CaregiverRole role;
    private final InvitationStatus status;
    private final Instant invitedAt;
    private final Instant acceptedAt;

    public ProfileCaregiver(
            Long id,
            Long accountId,
            String invitedEmail,
            CaregiverRole role,
            InvitationStatus status,
            Instant invitedAt,
            Instant acceptedAt) {
        this.id = id;
        this.accountId = accountId;
        this.invitedEmail = invitedEmail;
        this.role = role;
        this.status = status;
        this.invitedAt = invitedAt;
        this.acceptedAt = acceptedAt;
    }

    /** Invitación recién creada por el cuidador principal. */
    public static ProfileCaregiver invited(String email, CaregiverRole role, Instant now) {
        if (email == null || email.isBlank()) {
            throw new DomainRuleViolation(
                    "caregiver.email.required", "El correo del cuidador es obligatorio.", "email");
        }
        return new ProfileCaregiver(null, null, email.trim(), role, InvitationStatus.PENDING, now, null);
    }

    /** Cuidador principal que crea el perfil: nace ACTIVE con su cuenta. */
    public static ProfileCaregiver primary(Long accountId, String email, Instant now) {
        return new ProfileCaregiver(
                null, accountId, email, CaregiverRole.PRIMARY, InvitationStatus.ACTIVE, now, now);
    }

    public Long id() {
        return id;
    }

    public Long accountId() {
        return accountId;
    }

    public String invitedEmail() {
        return invitedEmail;
    }

    public CaregiverRole role() {
        return role;
    }

    public InvitationStatus status() {
        return status;
    }

    public Instant invitedAt() {
        return invitedAt;
    }

    public Instant acceptedAt() {
        return acceptedAt;
    }

    public boolean isPrincipal() {
        return role == CaregiverRole.PRIMARY;
    }

    public boolean isActive() {
        return status == InvitationStatus.ACTIVE;
    }

    /** Reasigna el id generado por la persistencia tras guardar. */
    public ProfileCaregiver withId(Long persistedId) {
        if (this.id != null) {
            return this;
        }
        return new ProfileCaregiver(
                persistedId, accountId, invitedEmail, role, status, invitedAt, acceptedAt);
    }

    /** Comando "Aceptar invitación": PENDING -> ACTIVE con la cuenta que acepta. */
    public ProfileCaregiver accept(Long acceptingAccountId) {
        if (status != InvitationStatus.PENDING) {
            throw DomainRuleViolation.conflict(
                    "caregiver.invitation.notPending",
                    "La invitación ya no está pendiente.",
                    "status");
        }
        if (acceptingAccountId == null) {
            throw new DomainRuleViolation(
                    "caregiver.account.required", "Falta la cuenta que acepta la invitación.",
                    "accountId");
        }
        return new ProfileCaregiver(
                id,
                acceptingAccountId,
                invitedEmail,
                role,
                InvitationStatus.ACTIVE,
                invitedAt,
                Instant.now());
    }

    /** Comando "Revocar acceso": el principal nunca se revoca (regla 1). */
    public ProfileCaregiver revoke() {
        if (isPrincipal()) {
            throw DomainRuleViolation.conflict(
                    "caregiver.principal.notRevocable",
                    "El cuidador principal no puede ser revocado.",
                    null);
        }
        if (status != InvitationStatus.ACTIVE) {
            throw DomainRuleViolation.conflict(
                    "caregiver.notActive",
                    "Solo se puede revocar un cuidador activo.",
                    "status");
        }
        return new ProfileCaregiver(
                id, accountId, invitedEmail, role, InvitationStatus.REVOKED, invitedAt, acceptedAt);
    }
}
