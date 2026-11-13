package com.acme.nubi.platform.assistivecomunication.domain.repositories;

import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramSelection;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.SelectionStatus;
import com.acme.nubi.platform.shared.domain.repositories.ICrudRepository;

import java.util.List;

public interface PictogramSelectionRepository extends ICrudRepository<PictogramSelection, Long> {
    /** Historial de selecciones del tablero, de la más reciente a la más antigua. */
    List<PictogramSelection> findAllByBoardId(Long boardId);

    List<PictogramSelection> findAllByBoardIdAndStatus(Long boardId, SelectionStatus status);
}
