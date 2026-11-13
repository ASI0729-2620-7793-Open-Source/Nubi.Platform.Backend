package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.SelectionStatus;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.PictogramSelectionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PictogramSelectionPersistenceRepository
        extends JpaRepository<PictogramSelectionPersistenceEntity, Long> {
    List<PictogramSelectionPersistenceEntity> findAllByBoardIdOrderBySelectedAtDesc(Long boardId);

    List<PictogramSelectionPersistenceEntity> findAllByBoardIdAndStatusOrderBySelectedAtDesc(
            Long boardId, SelectionStatus status);
}
