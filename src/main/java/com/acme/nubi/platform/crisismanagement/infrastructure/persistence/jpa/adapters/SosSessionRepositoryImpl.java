package com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SosSessionStatus;
import com.acme.nubi.platform.crisismanagement.domain.repositories.SosSessionRepository;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.mappers.SosSessionPersistenceMapper;
import com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.repositories.SosSessionPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SosSessionRepositoryImpl implements SosSessionRepository {

    private final SosSessionPersistenceRepository sosSessionPersistenceRepository;

    public SosSessionRepositoryImpl(SosSessionPersistenceRepository sosSessionPersistenceRepository) {
        this.sosSessionPersistenceRepository = sosSessionPersistenceRepository;
    }

    @Override
    public SosSession save(SosSession session) {
        var entity = session.getId() == null
                ? SosSessionPersistenceMapper.toPersistence(session)
                : update(session);
        return SosSessionPersistenceMapper.toDomain(sosSessionPersistenceRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<SosSession> findById(Long id) {
        return sosSessionPersistenceRepository.findById(id).map(SosSessionPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return sosSessionPersistenceRepository.existsById(id);
    }

    @Override
    public Optional<SosSession> findActiveByProfileId(ProfileId profileId) {
        return sosSessionPersistenceRepository
                .findFirstByProfileIdAndStatusOrderByStartedAtDescIdDesc(profileId.value(), SosSessionStatus.IN_PROGRESS)
                .map(SosSessionPersistenceMapper::toDomain);
    }

    @Override
    public List<SosSession> findAllByProfileId(ProfileId profileId) {
        return sosSessionPersistenceRepository.findAllByProfileIdOrderByStartedAtDescIdDesc(profileId.value()).stream()
                .map(SosSessionPersistenceMapper::toDomain)
                .toList();
    }

    private com.acme.nubi.platform.crisismanagement.infrastructure.persistence.jpa.entities.SosSessionPersistenceEntity
    update(SosSession session) {
        var entity = sosSessionPersistenceRepository.findById(session.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "The SOS session %d does not exist.".formatted(session.getId())));
        SosSessionPersistenceMapper.apply(entity, session);
        return entity;
    }
}
