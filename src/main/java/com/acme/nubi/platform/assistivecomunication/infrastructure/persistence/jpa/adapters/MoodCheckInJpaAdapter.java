package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.assistivecomunication.domain.model.entities.MoodCheckIn;
import com.acme.nubi.platform.assistivecomunication.domain.repositories.MoodCheckInRepository;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.CommunicationBoardPersistenceEntity;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.MoodCheckInPersistenceEntity;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.mappers.CommunicationPersistenceMapper;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.repositories.MoodCheckInPersistenceRepository;
import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class MoodCheckInJpaAdapter implements MoodCheckInRepository {
    private final MoodCheckInPersistenceRepository repository;
    private final CommunicationPersistenceMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public MoodCheckInJpaAdapter(MoodCheckInPersistenceRepository repository,
                                 CommunicationPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public MoodCheckIn save(MoodCheckIn moodCheckIn) {
        MoodCheckInPersistenceEntity entity;
        if (moodCheckIn.getId() == null) {
            entity = new MoodCheckInPersistenceEntity();
            entity.setBoard(entityManager.getReference(CommunicationBoardPersistenceEntity.class, moodCheckIn.getBoardId()));
        } else {
            entity = repository.findById(moodCheckIn.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Check-in de ánimo", moodCheckIn.getId()));
        }
        mapper.apply(moodCheckIn, entity);
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MoodCheckIn> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodCheckIn> findAllByBoardId(Long boardId) {
        return repository.findAllByBoardIdOrderByCheckedAtDesc(boardId).stream().map(mapper::toDomain).toList();
    }
}
