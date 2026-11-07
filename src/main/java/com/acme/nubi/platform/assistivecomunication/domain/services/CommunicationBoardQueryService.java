package com.acme.nubi.platform.assistivecomunication.domain.services;

import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.CommunicationBoard;
import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.Pictogram;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.MoodCheckIn;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramSelection;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetBoardByProfileIdQuery;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetMoodCheckInsByProfileIdQuery;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetQuickAccessQuery;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetSelectionsByProfileIdQuery;

import java.util.List;

/** Casos de uso de lectura del tablero de Comunicación Asistida. */
public interface CommunicationBoardQueryService {
    CommunicationBoard handle(GetBoardByProfileIdQuery query);

    List<Pictogram> handle(GetQuickAccessQuery query);

    List<PictogramSelection> handle(GetSelectionsByProfileIdQuery query);

    List<MoodCheckIn> handle(GetMoodCheckInsByProfileIdQuery query);
}
