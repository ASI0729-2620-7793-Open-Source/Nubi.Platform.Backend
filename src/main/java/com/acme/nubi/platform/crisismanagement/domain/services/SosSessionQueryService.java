package com.acme.nubi.platform.crisismanagement.domain.services;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetActiveSosSessionByProfileIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetSosSessionByIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetSosSessionsByProfileIdQuery;

import java.util.List;
import java.util.Optional;

public interface SosSessionQueryService {
    Optional<SosSession> handle(GetSosSessionByIdQuery query);

    Optional<SosSession> handle(GetActiveSosSessionByProfileIdQuery query);

    List<SosSession> handle(GetSosSessionsByProfileIdQuery query);
}
