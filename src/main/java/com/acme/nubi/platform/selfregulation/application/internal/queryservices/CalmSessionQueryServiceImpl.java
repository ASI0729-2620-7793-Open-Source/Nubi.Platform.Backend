package com.acme.nubi.platform.selfregulation.application.internal.queryservices;

import com.acme.nubi.platform.selfregulation.domain.model.aggregates.CalmSession;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmSessionByIdQuery;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetCalmSessionsByProfileIdQuery;
import com.acme.nubi.platform.selfregulation.domain.model.queries.GetLatestCalmSessionByProfileIdQuery;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.domain.repositories.CalmSessionRepository;
import com.acme.nubi.platform.selfregulation.domain.services.CalmSessionQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CalmSessionQueryServiceImpl implements CalmSessionQueryService {

    private final CalmSessionRepository calmSessionRepository;

    public CalmSessionQueryServiceImpl(CalmSessionRepository calmSessionRepository) {
        this.calmSessionRepository = calmSessionRepository;
    }

    @Override
    public Optional<CalmSession> handle(GetCalmSessionByIdQuery query) {
        return calmSessionRepository.findById(query.calmSessionId());
    }

    @Override
    public List<CalmSession> handle(GetCalmSessionsByProfileIdQuery query) {
        return calmSessionRepository.findAllByProfileId(new ProfileId(query.profileId()));
    }

    @Override
    public Optional<CalmSession> handle(GetLatestCalmSessionByProfileIdQuery query) {
        return calmSessionRepository.findLatestByProfileId(new ProfileId(query.profileId()));
    }
}
