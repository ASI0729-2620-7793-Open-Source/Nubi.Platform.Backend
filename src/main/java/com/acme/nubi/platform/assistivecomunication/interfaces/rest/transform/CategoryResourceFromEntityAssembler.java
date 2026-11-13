package com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform;

import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramCategory;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.CategoryResource;

public class CategoryResourceFromEntityAssembler {
    private CategoryResourceFromEntityAssembler() {
    }

    public static CategoryResource toResourceFromEntity(PictogramCategory category) {
        return new CategoryResource(category.getId(), category.getName(), category.getType().name(),
                category.getDisplayOrder());
    }
}
