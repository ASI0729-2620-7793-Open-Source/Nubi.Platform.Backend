package com.acme.nubi.platform.supportnetwork.application.internal.queryservices;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.HelpRequest;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetAllHelpRequestsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetHelpRequestByIdQuery;
import com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories.HelpRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class HelpRequestQueryServiceImpl {

    private final HelpRequestRepository helpRequestRepository;

    public HelpRequestQueryServiceImpl(HelpRequestRepository helpRequestRepository) {
        this.helpRequestRepository = helpRequestRepository;
    }

    public Optional<HelpRequest> handle(GetHelpRequestByIdQuery query) {
        return helpRequestRepository.findById(query.id());
    }

    public List<HelpRequest> handle(GetAllHelpRequestsByProfileIdQuery query) {
        return helpRequestRepository.findAllByProfileId(query.profileId());
    }
}
