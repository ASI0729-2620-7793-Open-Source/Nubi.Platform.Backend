package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.MoodCheckInPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoodCheckInPersistenceRepository extends JpaRepository<MoodCheckInPersistenceEntity, Long> {
    List<MoodCheckInPersistenceEntity> findAllByBoardIdOrderByCheckedAtDesc(Long boardId);
}
