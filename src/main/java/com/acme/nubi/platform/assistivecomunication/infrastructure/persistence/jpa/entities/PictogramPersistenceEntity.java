package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pictograms",
        indexes = @Index(name = "ix_pictograms_board_id_favorite", columnList = "board_id, favorite"))
@Getter
@Setter
@NoArgsConstructor
public class PictogramPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private CommunicationBoardPersistenceEntity board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private PictogramCategoryPersistenceEntity category;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    /** Frase que se reproduce en voz alta. */
    @Column(name = "phrase", nullable = false, length = 255)
    private String phrase;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "favorite", nullable = false)
    private boolean favorite;

    /** Verdadero si lo creó el cuidador; falso si es uno de los pictogramas básicos. */
    @Column(name = "custom", nullable = false)
    private boolean custom;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
