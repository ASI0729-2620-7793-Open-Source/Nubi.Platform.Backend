package com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.adapters;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmingResource;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ResourceType;
import com.acme.nubi.platform.selfregulation.domain.repositories.CalmingResourceRepository;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.mappers.CalmingResourcePersistenceMapper;
import com.acme.nubi.platform.selfregulation.infrastructure.persistence.jpa.repositories.CalmingResourcePersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CalmingResourceRepositoryImpl implements CalmingResourceRepository {

    private final CalmingResourcePersistenceRepository calmingResourcePersistenceRepository;

    public CalmingResourceRepositoryImpl(CalmingResourcePersistenceRepository calmingResourcePersistenceRepository) {
        this.calmingResourcePersistenceRepository = calmingResourcePersistenceRepository;
    }

    @Override
    public CalmingResource save(CalmingResource resource) {
        var saved = calmingResourcePersistenceRepository.save(CalmingResourcePersistenceMapper.toPersistence(resource));
        return CalmingResourcePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<CalmingResource> findById(Long id) {
        return calmingResourcePersistenceRepository.findById(id).map(CalmingResourcePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return calmingResourcePersistenceRepository.existsById(id);
    }

    @Override
    public List<CalmingResource> findAllActive() {
        return calmingResourcePersistenceRepository.findAllByActiveTrueOrderByIdAsc().stream()
                .map(CalmingResourcePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<CalmingResource> findAllActiveByType(ResourceType type) {
        return calmingResourcePersistenceRepository.findAllByActiveTrueAndTypeOrderByIdAsc(type).stream()
                .map(CalmingResourcePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCode(String code) {
        return calmingResourcePersistenceRepository.existsByCode(code);
    }
}
