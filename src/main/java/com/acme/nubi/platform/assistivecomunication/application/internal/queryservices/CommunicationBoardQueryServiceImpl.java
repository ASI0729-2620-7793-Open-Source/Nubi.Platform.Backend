package com.acme.nubi.platform.assistivecomunication.application.internal.queryservices;

import com.acme.nubi.platform.assistivecomunication.application.internal.DefaultBoardProvisioner;
import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.CommunicationBoard;
import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.Pictogram;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.MoodCheckIn;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramSelection;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetBoardByProfileIdQuery;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetMoodCheckInsByProfileIdQuery;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetQuickAccessQuery;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetSelectionsByProfileIdQuery;
import com.acme.nubi.platform.assistivecomunication.domain.repositories.MoodCheckInRepository;
import com.acme.nubi.platform.assistivecomunication.domain.repositories.PictogramSelectionRepository;
import com.acme.nubi.platform.assistivecomunication.domain.services.CommunicationBoardQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommunicationBoardQueryServiceImpl implements CommunicationBoardQueryService {
    private final DefaultBoardProvisioner boardProvisioner;
    private final PictogramSelectionRepository selectionRepository;
    private final MoodCheckInRepository moodCheckInRepository;

    public CommunicationBoardQueryServiceImpl(DefaultBoardProvisioner boardProvisioner,
                                              PictogramSelectionRepository selectionRepository,
                                              MoodCheckInRepository moodCheckInRepository) {
        this.boardProvisioner = boardProvisioner;
        this.selectionRepository = selectionRepository;
        this.moodCheckInRepository = moodCheckInRepository;
    }

    @Override
    public CommunicationBoard handle(GetBoardByProfileIdQuery query) {
        return boardProvisioner.findOrProvision(query.profileId());
    }

    @Override
    public List<Pictogram> handle(GetQuickAccessQuery query) {
        return boardProvisioner.findOrProvision(query.profileId()).getQuickAccess();
    }

    @Override
    public List<PictogramSelection> handle(GetSelectionsByProfileIdQuery query) {
        CommunicationBoard board = boardProvisioner.findOrProvision(query.profileId());
        return query.status() == null
                ? selectionRepository.findAllByBoardId(board.getId())
                : selectionRepository.findAllByBoardIdAndStatus(board.getId(), query.status());
    }

    @Override
    public List<MoodCheckIn> handle(GetMoodCheckInsByProfileIdQuery query) {
        CommunicationBoard board = boardProvisioner.findOrProvision(query.profileId());
        return moodCheckInRepository.findAllByBoardId(board.getId());
    }
}
