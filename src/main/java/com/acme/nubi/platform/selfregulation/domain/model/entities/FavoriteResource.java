package com.acme.nubi.platform.selfregulation.domain.model.entities;

import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Recurso de calma que un perfil marcó como favorito.
 */
@Getter
public class FavoriteResource {

    private Long id;
    private final ProfileId profileId;
    private final Long resourceId;
    private final Instant markedAt;

    public FavoriteResource(ProfileId profileId, Long resourceId, Instant markedAt) {
        this(null, profileId, resourceId, markedAt);
    }

    public FavoriteResource(Long id, ProfileId profileId, Long resourceId, Instant markedAt) {
        this.id = id;
        this.profileId = Objects.requireNonNull(profileId, "The profile id is required.");
        this.resourceId = Objects.requireNonNull(resourceId, "The calming resource id is required.");
        this.markedAt = Objects.requireNonNull(markedAt, "The marked time is required.");
    }
}
