package com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Entidad JPA del contacto de confianza (red de escalamiento de alertas). */
@Getter
@Setter
@Entity
@Table(name = "trusted_contact", schema = "profile")
public class TrustedContactJpaEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileJpaEntity profile;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phone;

    @Column
    private String email;

    @Column
    private String relationship;

    /** Menor número = se avisa primero cuando el anterior no confirma. */
    @Column(name = "priority_order", nullable = false)
    private int priorityOrder;
}
