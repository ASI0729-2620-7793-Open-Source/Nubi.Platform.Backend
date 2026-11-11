package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmSession;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.domain.repositories.CalmSessionRepository;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.mappers.CalmSessionPersistenceMapper;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.repositories.CalmSessionPersistenceRepository;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.repositories.CalmingResourcePersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CalmSessionRepositoryImpl implements CalmSessionRepository {

    private final CalmSessionPersistenceRepository calmSessionPersistenceRepository;
    private final CalmingResourcePersistenceRepository calmingResourcePersistenceRepository;

    public CalmSessionRepositoryImpl(CalmSessionPersistenceRepository calmSessionPersistenceRepository,
                                     CalmingResourcePersistenceRepository calmingResourcePersistenceRepository) {
        this.calmSessionPersistenceRepository = calmSessionPersistenceRepository;
        this.calmingResourcePersistenceRepository = calmingResourcePersistenceRepository;
    }

    @Override
    public CalmSession save(CalmSession session) {
        var resource = session.getResourceId() == null
                ? null
                : calmingResourcePersistenceRepository.getReferenceById(session.getResourceId());
        var saved = calmSessionPersistenceRepository.save(CalmSessionPersistenceMapper.toPersistence(session, resource));
        return CalmSessionPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<CalmSession> findById(Long id) {
        return calmSessionPersistenceRepository.findById(id).map(CalmSessionPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return calmSessionPersistenceRepository.existsById(id);
    }

    @Override
    public List<CalmSession> findAllByProfileId(ProfileId profileId) {
        return calmSessionPersistenceRepository.findAllByProfileIdOrderByStartedAtDescIdDesc(profileId.value()).stream()
                .map(CalmSessionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<CalmSession> findLatestByProfileId(ProfileId profileId) {
        return calmSessionPersistenceRepository.findFirstByProfileIdOrderByStartedAtDescIdDesc(profileId.value())
                .map(CalmSessionPersistenceMapper::toDomain);
    }
}
