package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.converters.ProfileIdPersistenceConverter;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "communication_boards")
@Getter
@Setter
@NoArgsConstructor
public class CommunicationBoardPersistenceEntity extends AuditableAbstractPersistenceEntity {
    /** Referencia lógica al perfil (sin clave foránea: es de otro Bounded Context). */
    @Convert(converter = ProfileIdPersistenceConverter.class)
    @Column(name = "profile_id", nullable = false, unique = true)
    private ProfileId profileId;

    @Column(name = "max_favorites", nullable = false)
    private Integer maxFavorites;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<PictogramCategoryPersistenceEntity> categories = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<PictogramPersistenceEntity> pictograms = new ArrayList<>();
}
