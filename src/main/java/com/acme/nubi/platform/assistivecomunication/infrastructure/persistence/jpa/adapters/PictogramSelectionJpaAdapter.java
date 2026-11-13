package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramSelection;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.SelectionStatus;
import com.acme.nubi.platform.assistivecomunication.domain.repositories.PictogramSelectionRepository;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.CommunicationBoardPersistenceEntity;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.PictogramPersistenceEntity;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.PictogramSelectionPersistenceEntity;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.mappers.CommunicationPersistenceMapper;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.repositories.PictogramSelectionPersistenceRepository;
import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class PictogramSelectionJpaAdapter implements PictogramSelectionRepository {
    private final PictogramSelectionPersistenceRepository repository;
    private final CommunicationPersistenceMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public PictogramSelectionJpaAdapter(PictogramSelectionPersistenceRepository repository,
                                        CommunicationPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PictogramSelection save(PictogramSelection selection) {
        PictogramSelectionPersistenceEntity entity;
        if (selection.getId() == null) {
            entity = new PictogramSelectionPersistenceEntity();
            entity.setBoard(entityManager.getReference(CommunicationBoardPersistenceEntity.class, selection.getBoardId()));
            entity.setPictogram(entityManager.getReference(PictogramPersistenceEntity.class, selection.getPictogramId()));
        } else {
            entity = repository.findById(selection.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Selección", selection.getId()));
        }
        mapper.apply(selection, entity);
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PictogramSelection> findById(Long id) {
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
    public List<PictogramSelection> findAllByBoardId(Long boardId) {
        return repository.findAllByBoardIdOrderBySelectedAtDesc(boardId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PictogramSelection> findAllByBoardIdAndStatus(Long boardId, SelectionStatus status) {
        return repository.findAllByBoardIdAndStatusOrderBySelectedAtDesc(boardId, status).stream()
                .map(mapper::toDomain).toList();
    }
}
