package com.acme.nubi.platform.selfregulation.application.internal.eventhandlers;

import com.acme.nubi.platform.selfregulation.domain.model.commands.SeedCalmingResourcesCommand;
import com.acme.nubi.platform.selfregulation.domain.services.CalmingResourceCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Registra el catálogo inicial de recursos de calma cuando la aplicación termina de arrancar.
 */
@Service("selfRegulationApplicationReadyEventHandler")
public class ApplicationReadyEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    private final CalmingResourceCommandService calmingResourceCommandService;

    public ApplicationReadyEventHandler(CalmingResourceCommandService calmingResourceCommandService) {
        this.calmingResourceCommandService = calmingResourceCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        calmingResourceCommandService.handle(new SeedCalmingResourcesCommand());
        LOGGER.info("Calming resources catalog verified for {}", event.getApplicationContext().getId());
    }
}
