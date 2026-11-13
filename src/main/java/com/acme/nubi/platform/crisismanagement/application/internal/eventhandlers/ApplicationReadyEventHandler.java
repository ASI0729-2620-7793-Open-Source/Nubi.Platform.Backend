package com.acme.nubi.platform.crisismanagement.application.internal.eventhandlers;

import com.acme.nubi.platform.crisismanagement.domain.model.commands.SeedActionGuideCommand;
import com.acme.nubi.platform.crisismanagement.domain.services.ActionGuideCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Registra la guía de contención inicial cuando la aplicación termina de arrancar.
 */
@Service("crisisManagementApplicationReadyEventHandler")
public class ApplicationReadyEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    private final ActionGuideCommandService actionGuideCommandService;

    public ApplicationReadyEventHandler(ActionGuideCommandService actionGuideCommandService) {
        this.actionGuideCommandService = actionGuideCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        actionGuideCommandService.handle(new SeedActionGuideCommand());
        LOGGER.info("Action guide verified for {}", event.getApplicationContext().getId());
    }
}
