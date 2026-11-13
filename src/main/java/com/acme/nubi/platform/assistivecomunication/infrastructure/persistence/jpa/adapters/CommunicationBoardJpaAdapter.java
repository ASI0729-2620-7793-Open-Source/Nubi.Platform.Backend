package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.CommunicationBoard;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.assistivecomunication.domain.repositories.CommunicationBoardRepository;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.CommunicationBoardPersistenceEntity;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.mappers.CommunicationPersistenceMapper;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.repositories.CommunicationBoardPersistenceRepository;
import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Implementa el repositorio de dominio del tablero con Spring Data JPA. */
@Repository
@Transactional
public class CommunicationBoardJpaAdapter implements CommunicationBoardRepository {
    private final CommunicationBoardPersistenceRepository repository;
    private final CommunicationPersistenceMapper mapper;

    public CommunicationBoardJpaAdapter(CommunicationBoardPersistenceRepository repository,
                                        CommunicationPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CommunicationBoard save(CommunicationBoard board) {
        CommunicationBoardPersistenceEntity entity;
        if (board.getId() == null) {
            entity = mapper.toNewEntity(board);
        } else {
            entity = repository.findById(board.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tablero", board.getId()));
            mapper.apply(board, entity);
        }
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommunicationBoard> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommunicationBoard> findByProfileId(ProfileId profileId) {
        return repository.findByProfileId(profileId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProfileId(ProfileId profileId) {
        return repository.existsByProfileId(profileId);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
