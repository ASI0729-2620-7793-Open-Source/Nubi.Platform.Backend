package com.acme.nubi.platform.supportnetwork.application.internal.commandservices;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.EpisodeReport;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateEpisodeReportCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.MarkEpisodeReportAsSharedCommand;
import com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories.EpisodeReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EpisodeReportCommandServiceImpl {

    private final EpisodeReportRepository episodeReportRepository;

    public EpisodeReportCommandServiceImpl(EpisodeReportRepository episodeReportRepository) {
        this.episodeReportRepository = episodeReportRepository;
    }

    public EpisodeReport handle(CreateEpisodeReportCommand command) {
        return episodeReportRepository.save(EpisodeReport.create(command));
    }

    public EpisodeReport handle(MarkEpisodeReportAsSharedCommand command) {
        EpisodeReport episodeReport = episodeReportRepository.findById(command.episodeReportId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Episode report not found: " + command.episodeReportId()));
        episodeReport.markAsShared(command);
        return episodeReportRepository.save(episodeReport);
    }
}
