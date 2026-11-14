package com.acme.nubi.platform.supportnetwork.domain.model.commands;

public record CreateHelpRequestCommand(Long profileId, String message) {
}
