package com.acme.nubi.platform.supportnetwork.domain.model.commands;

public record MarkEpisodeReportAsSharedCommand(Long episodeReportId, String sharedWith) {
}
