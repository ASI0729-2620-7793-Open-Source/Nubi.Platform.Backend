package com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.profile.domain.model.enums.ConditionType;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** Entidad JPA del diagnóstico declarado por el cuidador (US-04). */
@Getter
@Setter
@Entity
@Table(name = "declared_diagnosis")
public class DeclaredDiagnosisJpaEntity extends AuditableAbstractPersistenceEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private ProfileJpaEntity profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private ConditionType conditionType;

    /** Relleno solo cuando conditionType = OTHER. */
    @Column(name = "custom_description")
    private String customDescription;

    @Column(name = "diagnosed_by")
    private String diagnosedBy;

    @Column(name = "diagnosis_date")
    private LocalDate diagnosisDate;

    @Column(name = "professional_notes", length = 2000)
    private String professionalNotes;
}
