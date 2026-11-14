package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "favorite_resources",
        uniqueConstraints = @UniqueConstraint(name = "uq_favorite_resources_profile_id_resource_id",
                columnNames = {"profile_id", "resource_id"}))
@Getter
@Setter
@NoArgsConstructor
public class FavoriteResourcePersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Referencia lógica a Perfil y Personalización, sin clave foránea. */
    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_favorite_resources_resource"))
    private CalmingResourcePersistenceEntity resource;

    @Column(name = "marked_at", nullable = false)
    private Instant markedAt;
}
