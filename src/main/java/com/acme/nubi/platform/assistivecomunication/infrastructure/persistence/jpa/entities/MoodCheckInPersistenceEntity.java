package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mood_check_ins",
        indexes = @Index(name = "ix_mood_check_ins_board_id_checked_at", columnList = "board_id, checked_at"))
@Getter
@Setter
@NoArgsConstructor
public class MoodCheckInPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private CommunicationBoardPersistenceEntity board;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private EmotionalIntensityLevel level;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;
}
