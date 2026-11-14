package com.acme.nubi.platform.selfregulation.domain.model.errors;

import com.acme.nubi.platform.shared.domain.model.errors.DomainError;

public final class SelfRegulationErrors {

    private SelfRegulationErrors() {
    }

    public static DomainError calmSessionNotFound(Long calmSessionId) {
        return DomainError.notFound("SelfRegulation.CalmSessionNotFound",
                "The calm session %d was not found.".formatted(calmSessionId));
    }

    public static DomainError calmingResourceNotFound(Long calmingResourceId) {
        return DomainError.notFound("SelfRegulation.CalmingResourceNotFound",
                "The calming resource %d was not found.".formatted(calmingResourceId));
    }

    public static DomainError calmingResourceUnavailable(Long calmingResourceId) {
        return DomainError.conflict("SelfRegulation.CalmingResourceUnavailable",
                "The calming resource %d is not available.".formatted(calmingResourceId));
    }

    public static DomainError profileNotFound(Long profileId) {
        return DomainError.notFound("SelfRegulation.ProfileNotFound",
                "The profile %d was not found.".formatted(profileId));
    }

    public static DomainError favoriteResourceNotFound(Long profileId, Long calmingResourceId) {
        return DomainError.notFound("SelfRegulation.FavoriteResourceNotFound",
                "The calming resource %d is not a favorite of the profile %d.".formatted(calmingResourceId, profileId));
    }

    public static DomainError favoriteResourceAlreadyMarked(Long profileId, Long calmingResourceId) {
        return DomainError.conflict("SelfRegulation.FavoriteResourceAlreadyMarked",
                "The calming resource %d is already a favorite of the profile %d.".formatted(calmingResourceId, profileId));
    }

    public static DomainError invalidCalmSessionOperation(String message) {
        return DomainError.conflict("SelfRegulation.InvalidCalmSessionOperation", message);
    }

    public static DomainError invalidCalmSessionData(String message) {
        return DomainError.validation("SelfRegulation.InvalidCalmSessionData", message);
    }
}
