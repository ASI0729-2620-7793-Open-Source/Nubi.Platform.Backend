package com.acme.nubi.platform.crisismanagement.domain.services;

import com.acme.nubi.platform.crisismanagement.domain.model.commands.SeedActionGuideCommand;

public interface ActionGuideCommandService {
    void handle(SeedActionGuideCommand command);
}
