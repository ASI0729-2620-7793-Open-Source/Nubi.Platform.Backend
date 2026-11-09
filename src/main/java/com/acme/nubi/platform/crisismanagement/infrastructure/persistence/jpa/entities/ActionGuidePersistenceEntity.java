package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "action_guides",
        indexes = @Index(name = "ix_action_guides_active", columnList = "active"))
@Getter
@Setter
@NoArgsConstructor
public class ActionGuidePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<ContainmentStepPersistenceEntity> steps = new ArrayList<>();
}
