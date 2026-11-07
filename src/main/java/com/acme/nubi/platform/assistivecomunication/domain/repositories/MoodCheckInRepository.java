package com.acme.nubi.platform.assistivecomunication.domain.repositories;

import com.acme.nubi.platform.assistivecomunication.domain.model.entities.MoodCheckIn;
import com.acme.nubi.platform.shared.domain.repositories.ICrudRepository;

import java.util.List;

public interface MoodCheckInRepository extends ICrudRepository<MoodCheckIn, Long> {
    /** Historial de check-ins del tablero, del más reciente al más antiguo. */
    List<MoodCheckIn> findAllByBoardId(Long boardId);
}
