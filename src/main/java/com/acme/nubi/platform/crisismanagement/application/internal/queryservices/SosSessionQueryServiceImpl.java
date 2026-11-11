package com.acme.nubi.platform.crisismanagement.application.internal.queryservices;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.SosSession;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetActiveSosSessionByProfileIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetSosSessionByIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetSosSessionsByProfileIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.repositories.SosSessionRepository;
import com.acme.nubi.platform.crisismanagement.domain.services.SosSessionQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SosSessionQueryServiceImpl implements SosSessionQueryService {

    private final SosSessionRepository sosSessionRepository;

    public SosSessionQueryServiceImpl(SosSessionRepository sosSessionRepository) {
        this.sosSessionRepository = sosSessionRepository;
    }

    @Override
    public Optional<SosSession> handle(GetSosSessionByIdQuery query) {
        return sosSessionRepository.findById(query.sosSessionId());
    }

    @Override
    public Optional<SosSession> handle(GetActiveSosSessionByProfileIdQuery query) {
        return sosSessionRepository.findActiveByProfileId(new ProfileId(query.profileId()));
    }

    @Override
    public List<SosSession> handle(GetSosSessionsByProfileIdQuery query) {
        return sosSessionRepository.findAllByProfileId(new ProfileId(query.profileId()));
    }
}
