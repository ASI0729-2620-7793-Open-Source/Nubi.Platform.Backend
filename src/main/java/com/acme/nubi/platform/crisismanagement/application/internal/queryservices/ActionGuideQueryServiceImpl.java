package com.acme.nubi.platform.crisismanagement.application.internal.queryservices;

import com.acme.nubi.platform.crisismanagement.domain.model.aggregates.ActionGuide;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetActionGuideByIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetActiveActionGuideQuery;
import com.acme.nubi.platform.crisismanagement.domain.repositories.ActionGuideRepository;
import com.acme.nubi.platform.crisismanagement.domain.services.ActionGuideQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ActionGuideQueryServiceImpl implements ActionGuideQueryService {

    private final ActionGuideRepository actionGuideRepository;

    public ActionGuideQueryServiceImpl(ActionGuideRepository actionGuideRepository) {
        this.actionGuideRepository = actionGuideRepository;
    }

    @Override
    public Optional<ActionGuide> handle(GetActiveActionGuideQuery query) {
        return actionGuideRepository.findActiveGuide();
    }

    @Override
    public Optional<ActionGuide> handle(GetActionGuideByIdQuery query) {
        return actionGuideRepository.findById(query.actionGuideId());
    }
}
