package com.acme.nubi.platform.assistivecomunication.application.internal;

import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.CommunicationBoard;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.assistivecomunication.domain.repositories.CommunicationBoardRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Devuelve el tablero del perfil o lo crea con las categorías y pictogramas básicos
 * si todavía no está configurado (US-19, escenario 2).
 */
@Component
public class DefaultBoardProvisioner {
    private final CommunicationBoardRepository boardRepository;

    public DefaultBoardProvisioner(CommunicationBoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional
    public CommunicationBoard findOrProvision(Long profileId) {
        ProfileId id = new ProfileId(profileId);
        return boardRepository.findByProfileId(id).orElseGet(() -> {
            // Primero se guardan las categorías para tener sus ids y luego se cargan los pictogramas
            CommunicationBoard board = boardRepository.save(CommunicationBoard.createDefault(id));
            board.loadDefaultPictograms();
            return boardRepository.save(board);
        });
    }
}
