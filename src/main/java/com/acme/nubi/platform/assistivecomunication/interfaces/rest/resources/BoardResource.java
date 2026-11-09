package com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources;

import java.util.List;

public record BoardResource(Long id, Long profileId, Integer maxFavorites, List<CategoryResource> categories,
                            List<PictogramResource> pictograms) {
}
