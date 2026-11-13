package com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform;

import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.CommunicationBoard;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.BoardResource;

public class BoardResourceFromEntityAssembler {
    private BoardResourceFromEntityAssembler() {
    }

    public static BoardResource toResourceFromEntity(CommunicationBoard board) {
        return new BoardResource(
                board.getId(),
                board.getProfileId().profileId(),
                board.getMaxFavorites(),
                board.getCategories().stream().map(CategoryResourceFromEntityAssembler::toResourceFromEntity).toList(),
                board.getPictograms().stream().map(PictogramResourceFromEntityAssembler::toResourceFromEntity).toList());
    }
}
