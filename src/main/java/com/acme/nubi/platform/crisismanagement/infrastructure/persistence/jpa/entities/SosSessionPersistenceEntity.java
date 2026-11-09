package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ObservedState;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SosSessionStatus;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sos_sessions",
        indexes = {
                @Index(name = "ix_sos_sessions_profile_id", columnList = "profile_id"),
                @Index(name = "ix_sos_sessions_profile_id_status", columnList = "profile_id, status")
        })
@Getter
@Setter
@NoArgsConstructor
public class SosSessionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    /** Referencia lógica a Perfil y Personalización, sin clave foránea. */
    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    /** Referencia lógica a la cuenta del cuidador (IAM), sin clave foránea. */
    @Column(name = "caregiver_account_id", nullable = false)
    private Long caregiverAccountId;

    @Column(name = "guide_id", nullable = false)
    private Long guideId;

    /** Solo lectura: existe para declarar la clave foránea hacia la guía. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_sos_sessions_guide"))
    private ActionGuidePersistenceEntity guide;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SosSessionStatus status;

    @Column(name = "current_step_order", nullable = false)
    private int currentStepOrder;

    @Column(name = "total_steps", nullable = false)
    private int totalSteps;

    @Enumerated(EnumType.STRING)
    @Column(name = "initial_intensity", nullable = false, length = 20)
    private EmotionalIntensityLevel initialIntensity;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_intensity", length = 20)
    private EmotionalIntensityLevel finalIntensity;

    /** "trigger" es palabra reservada en MySQL, por eso la columna se llama sensory_trigger. */
    @Enumerated(EnumType.STRING)
    @Column(name = "sensory_trigger", nullable = false, length = 20)
    private SensoryTrigger trigger;

    @Enumerated(EnumType.STRING)
    @Column(name = "observed_state", length = 20)
    private ObservedState observedState;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("completedAt ASC, id ASC")
    private List<CompletedStepPersistenceEntity> completedSteps = new ArrayList<>();
}
