package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ResourceType;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "calming_resources",
        uniqueConstraints = @UniqueConstraint(name = "uq_calming_resources_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
public class CalmingResourcePersistenceEntity extends AuditableAbstractPersistenceEntity {

    /** Identificador estable que usa el frontend para sus textos traducidos. */
    @Column(name = "code", nullable = false, length = 60)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType type;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "max_intensity", nullable = false)
    private int maxIntensity;

    @Column(name = "available_offline", nullable = false)
    private boolean availableOffline;

    @Column(name = "active", nullable = false)
    private boolean active;
}
