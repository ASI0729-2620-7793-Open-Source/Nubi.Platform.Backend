package com.acme.nubi.platform.supportnetwork.application.internal.commandservices;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.HelpRequest;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.CancelHelpRequestCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.ConfirmHelpRequestCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateHelpRequestCommand;
import com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories.HelpRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HelpRequestCommandServiceImpl {

    private final HelpRequestRepository helpRequestRepository;

    public HelpRequestCommandServiceImpl(HelpRequestRepository helpRequestRepository) {
        this.helpRequestRepository = helpRequestRepository;
    }

    public HelpRequest handle(CreateHelpRequestCommand command) {
        return helpRequestRepository.save(HelpRequest.create(command));
    }

    public HelpRequest handle(ConfirmHelpRequestCommand command) {
        HelpRequest helpRequest = findById(command.helpRequestId());
        helpRequest.confirm(command);
        return helpRequestRepository.save(helpRequest);
    }

    public HelpRequest handle(CancelHelpRequestCommand command) {
        HelpRequest helpRequest = findById(command.helpRequestId());
        helpRequest.cancel(command);
        return helpRequestRepository.save(helpRequest);
    }

    private HelpRequest findById(Long id) {
        return helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Help request not found: " + id));
    }
}
