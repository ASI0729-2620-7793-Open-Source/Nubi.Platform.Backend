package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.CalmSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "calm_sessions",
        indexes = @Index(name = "ix_calm_sessions_profile_id", columnList = "profile_id"))
@Getter
@Setter
@NoArgsConstructor
public class CalmSessionPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Referencia lógica a Perfil y Personalización, sin clave foránea. */
    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", foreignKey = @ForeignKey(name = "fk_calm_sessions_resource"))
    private CalmingResourcePersistenceEntity resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CalmSessionStatus status;

    @Column(name = "intensity", nullable = false)
    private int intensity;

    @Column(name = "low_stimulation_active", nullable = false)
    private boolean lowStimulationActive;

    @Column(name = "started_by_sos", nullable = false)
    private boolean startedBySos;

    @Embedded
    private CalmTimerPersistenceEmbeddable timer = CalmTimerPersistenceEmbeddable.empty();

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;
}
