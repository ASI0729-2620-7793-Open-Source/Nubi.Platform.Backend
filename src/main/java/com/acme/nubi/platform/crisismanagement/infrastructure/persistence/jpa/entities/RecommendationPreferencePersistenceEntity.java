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

@Entity
@Table(name = "recommendation_preferences",
        uniqueConstraints = @UniqueConstraint(name = "uq_recommendation_preferences_recommendation_id_account_id",
                columnNames = {"recommendation_id", "account_id"}))
@Getter
@Setter
@NoArgsConstructor
public class RecommendationPreferencePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recommendation_preferences_recommendation"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RecommendationPersistenceEntity recommendation;

    /** Referencia lógica a la cuenta del cuidador (IAM), sin clave foránea. */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "usefulness_rating")
    private Integer usefulnessRating;

    @Column(name = "favorite", nullable = false)
    private boolean favorite;
}
