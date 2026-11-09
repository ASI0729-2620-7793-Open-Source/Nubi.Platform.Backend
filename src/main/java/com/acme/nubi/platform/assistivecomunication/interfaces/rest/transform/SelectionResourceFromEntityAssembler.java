package com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform;

import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramSelection;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.SelectionResource;

public class SelectionResourceFromEntityAssembler {
    private SelectionResourceFromEntityAssembler() {
    }

    public static SelectionResource toResourceFromEntity(PictogramSelection selection) {
        return new SelectionResource(selection.getId(), selection.getPictogramId(), selection.getStatus().name(),
                selection.getSelectedAt(), selection.getUnderstoodAt());
    }
}
