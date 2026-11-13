package com.acme.nubi.platform.selfregulation.domain.repositories;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmingResource;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ResourceType;
import com.acme.nubi.platform.shared.domain.repositories.ICrudRepository;

import java.util.List;

public interface CalmingResourceRepository extends ICrudRepository<CalmingResource, Long> {
    List<CalmingResource> findAllActive();

    List<CalmingResource> findAllActiveByType(ResourceType type);

    boolean existsByCode(String code);
}
