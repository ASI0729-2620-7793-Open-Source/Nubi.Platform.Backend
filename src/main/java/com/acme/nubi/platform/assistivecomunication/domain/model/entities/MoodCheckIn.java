package com.acme.nubi.platform.assistivecomunication.domain.model.entities;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.EmotionalIntensityLevel;
import lombok.Getter;

import java.time.LocalDateTime;

/** Registro del estado de ánimo del usuario. Solo se insertan registros nuevos. */
@Getter
public class MoodCheckIn {
    private Long id;
    private final Long boardId;
    private final EmotionalIntensityLevel level;
    private final LocalDateTime checkedAt;

    public MoodCheckIn(Long id, Long boardId, EmotionalIntensityLevel level, LocalDateTime checkedAt) {
        if (boardId == null || level == null || checkedAt == null) {
            throw new IllegalArgumentException("El check-in requiere tablero, nivel y fecha");
        }
        this.id = id;
        this.boardId = boardId;
        this.level = level;
        this.checkedAt = checkedAt;
    }

    public MoodCheckIn(Long boardId, EmotionalIntensityLevel level) {
        this(null, boardId, level, LocalDateTime.now());
    }
}
