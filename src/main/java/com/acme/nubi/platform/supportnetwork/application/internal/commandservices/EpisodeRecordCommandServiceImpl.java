package com.acme.nubi.platform.supportnetwork.application.internal.commandservices;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.EpisodeRecord;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.AddEpisodeNoteCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateEpisodeRecordCommand;
import com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories.EpisodeRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EpisodeRecordCommandServiceImpl {

    private final EpisodeRecordRepository episodeRecordRepository;

    public EpisodeRecordCommandServiceImpl(EpisodeRecordRepository episodeRecordRepository) {
        this.episodeRecordRepository = episodeRecordRepository;
    }

    public EpisodeRecord handle(CreateEpisodeRecordCommand command) {
        return episodeRecordRepository.save(EpisodeRecord.create(command));
    }

    public EpisodeRecord handle(AddEpisodeNoteCommand command) {
        EpisodeRecord episodeRecord = episodeRecordRepository.findById(command.episodeRecordId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Episode record not found: " + command.episodeRecordId()));
        episodeRecord.addNote(command);
        return episodeRecordRepository.save(episodeRecord);
    }
}
