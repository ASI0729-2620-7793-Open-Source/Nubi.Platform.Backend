package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.CategoryType;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pictogram_categories", schema = "communication",
        uniqueConstraints = @UniqueConstraint(name = "uq_pictogram_categories_board_id_name",
                columnNames = {"board_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
public class PictogramCategoryPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private CommunicationBoardPersistenceEntity board;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CategoryType type;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
