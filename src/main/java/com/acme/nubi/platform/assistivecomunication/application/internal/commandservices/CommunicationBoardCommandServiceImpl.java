package com.acme.nubi.platform.assistivecomunication.application.internal.commandservices;

import com.acme.nubi.platform.assistivecomunication.application.internal.DefaultBoardProvisioner;
import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.CommunicationBoard;
import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.Pictogram;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.ConfirmUnderstandingCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.CreateCategoryCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.CreatePictogramCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.MarkFavoriteCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.MarkNotUnderstoodCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.MarkSelectionDeliveredCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.MoveToCategoryCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.RegisterMoodCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.SelectPictogramCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.UnmarkFavoriteCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.MoodCheckIn;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramCategory;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramSelection;
import com.acme.nubi.platform.assistivecomunication.domain.model.events.PictogramSelectedEvent;
import com.acme.nubi.platform.assistivecomunication.domain.model.events.UnderstandingNotConfirmedEvent;
import com.acme.nubi.platform.assistivecomunication.domain.repositories.CommunicationBoardRepository;
import com.acme.nubi.platform.assistivecomunication.domain.repositories.MoodCheckInRepository;
import com.acme.nubi.platform.assistivecomunication.domain.repositories.PictogramSelectionRepository;
import com.acme.nubi.platform.assistivecomunication.domain.services.CommunicationBoardCommandService;
import com.acme.nubi.platform.shared.domain.events.DomainEventPublisher;
import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@Transactional
public class CommunicationBoardCommandServiceImpl implements CommunicationBoardCommandService {
    private final DefaultBoardProvisioner boardProvisioner;
    private final CommunicationBoardRepository boardRepository;
    private final PictogramSelectionRepository selectionRepository;
    private final MoodCheckInRepository moodCheckInRepository;
    private final DomainEventPublisher eventPublisher;

    public CommunicationBoardCommandServiceImpl(DefaultBoardProvisioner boardProvisioner,
                                                CommunicationBoardRepository boardRepository,
                                                PictogramSelectionRepository selectionRepository,
                                                MoodCheckInRepository moodCheckInRepository,
                                                DomainEventPublisher eventPublisher) {
        this.boardProvisioner = boardProvisioner;
        this.boardRepository = boardRepository;
        this.selectionRepository = selectionRepository;
        this.moodCheckInRepository = moodCheckInRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PictogramCategory handle(CreateCategoryCommand command) {
        CommunicationBoard board = boardProvisioner.findOrProvision(command.profileId());
        board.addCategory(command.name());
        CommunicationBoard saved = boardRepository.save(board);
        // La categoría recién creada es la de mayor id
        return saved.getCategories().stream()
                .max(Comparator.comparing(PictogramCategory::getId))
                .orElseThrow();
    }

    @Override
    public Pictogram handle(CreatePictogramCommand command) {
        CommunicationBoard board = boardProvisioner.findOrProvision(command.profileId());
        board.addPictogram(command.label(), command.phrase(), command.imageUrl(),
                command.audioUrl(), command.categoryId());
        CommunicationBoard saved = boardRepository.save(board);
        // El pictograma recién creado es el de mayor id
        return saved.getPictograms().stream()
                .max(Comparator.comparing(Pictogram::getId))
                .orElseThrow();
    }

    @Override
    public Pictogram handle(MarkFavoriteCommand command) {
        CommunicationBoard board = boardProvisioner.findOrProvision(command.profileId());
        board.markFavorite(command.pictogramId());
        return boardRepository.save(board).getPictogram(command.pictogramId());
    }

    @Override
    public Pictogram handle(UnmarkFavoriteCommand command) {
        CommunicationBoard board = boardProvisioner.findOrProvision(command.profileId());
        board.unmarkFavorite(command.pictogramId());
        return boardRepository.save(board).getPictogram(command.pictogramId());
    }

    @Override
    public Pictogram handle(MoveToCategoryCommand command) {
        CommunicationBoard board = boardProvisioner.findOrProvision(command.profileId());
        board.moveToCategory(command.pictogramId(), command.categoryId());
        return boardRepository.save(board).getPictogram(command.pictogramId());
    }

    @Override
    public PictogramSelection handle(SelectPictogramCommand command) {
        CommunicationBoard board = boardProvisioner.findOrProvision(command.profileId());
        Pictogram pictogram = board.getPictogram(command.pictogramId());
        PictogramSelection selection = selectionRepository.save(board.selectPictogram(command.pictogramId()));
        eventPublisher.publish(new PictogramSelectedEvent(
                selection.getId(), command.profileId(), pictogram.getPhrase(), selection.getSelectedAt()));
        return selection;
    }

    @Override
    public PictogramSelection handle(MarkSelectionDeliveredCommand command) {
        PictogramSelection selection = findSelection(command.profileId(), command.selectionId());
        selection.markAsDelivered();
        return selectionRepository.save(selection);
    }

    @Override
    public PictogramSelection handle(ConfirmUnderstandingCommand command) {
        PictogramSelection selection = findSelection(command.profileId(), command.selectionId());
        selection.confirmUnderstanding();
        return selectionRepository.save(selection);
    }

    @Override
    public PictogramSelection handle(MarkNotUnderstoodCommand command) {
        PictogramSelection selection = findSelection(command.profileId(), command.selectionId());
        selection.markAsNotUnderstood();
        PictogramSelection saved = selectionRepository.save(selection);
        eventPublisher.publish(new UnderstandingNotConfirmedEvent(
                saved.getId(), command.profileId(), LocalDateTime.now()));
        return saved;
    }

    @Override
    public MoodCheckIn handle(RegisterMoodCommand command) {
        CommunicationBoard board = boardProvisioner.findOrProvision(command.profileId());
        return moodCheckInRepository.save(board.registerMood(command.level()));
    }

    /** Busca la selección y comprueba que pertenece al tablero del perfil indicado. */
    private PictogramSelection findSelection(Long profileId, Long selectionId) {
        CommunicationBoard board = boardProvisioner.findOrProvision(profileId);
        return selectionRepository.findById(selectionId)
                .filter(selection -> selection.getBoardId().equals(board.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Selección", selectionId));
    }
}
