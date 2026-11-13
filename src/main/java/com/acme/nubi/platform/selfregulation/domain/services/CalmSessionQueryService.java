package com.acme.nubi.platform.selfregulation.domain.services;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmSession;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmSessionByIdQuery;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmSessionsByProfileIdQuery;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetLatestCalmSessionByProfileIdQuery;

import java.util.List;
import java.util.Optional;

public interface CalmSessionQueryService {
    Optional<CalmSession> handle(GetCalmSessionByIdQuery query);

    List<CalmSession> handle(GetCalmSessionsByProfileIdQuery query);

    Optional<CalmSession> handle(GetLatestCalmSessionByProfileIdQuery query);
}
