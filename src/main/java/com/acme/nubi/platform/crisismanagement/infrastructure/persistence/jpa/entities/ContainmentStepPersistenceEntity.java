package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "containment_steps",
        uniqueConstraints = @UniqueConstraint(name = "uq_containment_steps_guide_id_step_order",
                columnNames = {"guide_id", "step_order"}))
@Getter
@Setter
@NoArgsConstructor
public class ContainmentStepPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guide_id", nullable = false, foreignKey = @ForeignKey(name = "fk_containment_steps_guide"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ActionGuidePersistenceEntity guide;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "code", nullable = false, length = 60)
    private String code;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory;

    @Enumerated(EnumType.STRING)
    @Column(name = "related_trigger", nullable = false, length = 20)
    private SensoryTrigger relatedTrigger;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlternativeTechniquePersistenceEntity> alternativeTechniques = new ArrayList<>();
}
