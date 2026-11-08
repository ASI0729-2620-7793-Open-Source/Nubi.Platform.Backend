package com.acme.nubi.platform.crisismanagement.domain.services;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.ActionGuide;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetActionGuideByIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetActiveActionGuideQuery;

import java.util.Optional;

public interface ActionGuideQueryService {
    Optional<ActionGuide> handle(GetActiveActionGuideQuery query);

    Optional<ActionGuide> handle(GetActionGuideByIdQuery query);
}
