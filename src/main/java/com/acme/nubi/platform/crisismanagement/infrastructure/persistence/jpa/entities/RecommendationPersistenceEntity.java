package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.RecommendationTopic;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recommendations",
        indexes = @Index(name = "ix_recommendations_profile_id_topic", columnList = "profile_id, topic"),
        uniqueConstraints = @UniqueConstraint(name = "uq_recommendations_profile_id_topic_version",
                columnNames = {"profile_id", "topic", "version"}))
@Getter
@Setter
@NoArgsConstructor
public class RecommendationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    /** Referencia lógica a Perfil y Personalización, sin clave foránea. */
    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "topic", nullable = false, length = 20)
    private RecommendationTopic topic;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "outdated", nullable = false)
    private boolean outdated;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecommendationPreferencePersistenceEntity> preferences = new ArrayList<>();
}
