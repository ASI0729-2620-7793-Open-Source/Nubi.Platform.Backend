package com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.profile.domain.model.enums.CaregiverRole;
import com.acme.nubi.platform.profile.domain.model.enums.InvitationStatus;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** Entidad JPA del cuidador asociado a un perfil (US-06). */
@Getter
@Setter
@Entity
@Table(name = "profile_caregiver", schema = "profile")
public class ProfileCaregiverJpaEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileJpaEntity profile;

    /** Null mientras la invitación sigue pendiente. */
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "invited_email")
    private String invitedEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaregiverRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(name = "invited_at", nullable = false)
    private Instant invitedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;
}
