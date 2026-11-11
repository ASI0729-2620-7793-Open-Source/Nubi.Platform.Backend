package com.acme.nubi.platform.selfregulation.interfaces.rest.resources;

import java.time.Instant;

public record FavoriteResourceResource(Long id, Long profileId, Long resourceId, Instant markedAt) {
}
