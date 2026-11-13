package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "session_completed_steps",
        uniqueConstraints = @UniqueConstraint(name = "uq_session_completed_steps_session_id_step_id",
                columnNames = {"session_id", "step_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CompletedStepPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_session_completed_steps_session"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SosSessionPersistenceEntity session;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    /** Solo lectura: existe para declarar la clave foránea hacia el paso de la guía. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_session_completed_steps_step"))
    private ContainmentStepPersistenceEntity step;

    @Column(name = "skipped", nullable = false)
    private boolean skipped;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;
}
