package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "alternative_techniques")
@Getter
@Setter
@NoArgsConstructor
public class AlternativeTechniquePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "step_id", nullable = false, foreignKey = @ForeignKey(name = "fk_alternative_techniques_step"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ContainmentStepPersistenceEntity step;

    @Column(name = "code", nullable = false, length = 60)
    private String code;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 500)
    private String description;
}
