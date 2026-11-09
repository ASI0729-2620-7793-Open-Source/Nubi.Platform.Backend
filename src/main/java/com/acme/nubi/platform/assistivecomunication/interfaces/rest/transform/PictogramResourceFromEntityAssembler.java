package com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform;

import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.Pictogram;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.PictogramResource;

public class PictogramResourceFromEntityAssembler {
    private PictogramResourceFromEntityAssembler() {
    }

    public static PictogramResource toResourceFromEntity(Pictogram pictogram) {
        return new PictogramResource(pictogram.getId(), pictogram.getCategoryId(), pictogram.getLabel(),
                pictogram.getPhrase(), pictogram.getImageUrl(), pictogram.getAudioUrl(), pictogram.hasAudio(),
                pictogram.isFavorite(), pictogram.isCustom(), pictogram.getDisplayOrder());
    }
}
