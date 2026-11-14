package com.acme.nubi.platform.assistivecomunication.domain.services;

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

/** Casos de uso de escritura del tablero de Comunicación Asistida. */
public interface CommunicationBoardCommandService {
    PictogramCategory handle(CreateCategoryCommand command);

    Pictogram handle(CreatePictogramCommand command);

    Pictogram handle(MarkFavoriteCommand command);

    Pictogram handle(UnmarkFavoriteCommand command);

    Pictogram handle(MoveToCategoryCommand command);

    PictogramSelection handle(SelectPictogramCommand command);

    PictogramSelection handle(MarkSelectionDeliveredCommand command);

    PictogramSelection handle(ConfirmUnderstandingCommand command);

    PictogramSelection handle(MarkNotUnderstoodCommand command);

    MoodCheckIn handle(RegisterMoodCommand command);
}
