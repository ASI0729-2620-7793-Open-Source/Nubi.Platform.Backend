package com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.profile.domain.model.enums.PlanType;
import com.acme.nubi.platform.profile.domain.model.enums.SubscriptionStatus;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad JPA de la suscripción. Una cuenta tiene como máximo una fila aquí
 * (el índice único sobre account_id lo garantiza).
 */
@Getter
@Setter
@Entity
@Table(name = "subscription", schema = "profile")
public class SubscriptionJpaEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "account_id", nullable = false, unique = true)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ends_at")
    private Instant endsAt;
}
