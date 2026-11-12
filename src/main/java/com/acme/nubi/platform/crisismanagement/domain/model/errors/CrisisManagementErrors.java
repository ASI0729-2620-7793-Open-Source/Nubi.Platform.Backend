package com.acme.nubi.platform.crisismanagement.domain.model.errors;

import com.acme.nubi.platform.shared.domain.model.errors.DomainError;

public final class CrisisManagementErrors {

    private CrisisManagementErrors() {
    }

    public static DomainError sosSessionNotFound(Long sosSessionId) {
        return DomainError.notFound("CrisisManagement.SosSessionNotFound",
                "The SOS session %d was not found.".formatted(sosSessionId));
    }

    public static DomainError actionGuideNotFound(Long actionGuideId) {
        return DomainError.notFound("CrisisManagement.ActionGuideNotFound",
                "The action guide %d was not found.".formatted(actionGuideId));
    }

    public static DomainError activeActionGuideNotFound() {
        return DomainError.notFound("CrisisManagement.ActiveActionGuideNotFound",
                "There is no active action guide.");
    }

    public static DomainError stepNotFound(Long sosSessionId, Long stepId) {
        return DomainError.notFound("CrisisManagement.StepNotFound",
                "The step %d does not belong to the guide of the SOS session %d.".formatted(stepId, sosSessionId));
    }

    public static DomainError alternativeTechniqueNotFound(Long stepId) {
        return DomainError.notFound("CrisisManagement.AlternativeTechniqueNotFound",
                "The step %d has no alternative techniques.".formatted(stepId));
    }

    public static DomainError profileNotFound(Long profileId) {
        return DomainError.notFound("CrisisManagement.ProfileNotFound",
                "The profile %d was not found.".formatted(profileId));
    }

    public static DomainError activeSosSessionAlreadyExists(Long profileId) {
        return DomainError.conflict("CrisisManagement.ActiveSosSessionAlreadyExists",
                "The profile %d already has an SOS session in progress.".formatted(profileId));
    }

    public static DomainError invalidSosSessionOperation(String message) {
        return DomainError.conflict("CrisisManagement.InvalidSosSessionOperation", message);
    }

    public static DomainError invalidSosSessionData(String message) {
        return DomainError.validation("CrisisManagement.InvalidSosSessionData", message);
    }

    public static DomainError recommendationNotFound(Long recommendationId) {
        return DomainError.notFound("CrisisManagement.RecommendationNotFound",
                "The recommendation %d was not found.".formatted(recommendationId));
    }

    public static DomainError invalidRecommendationOperation(String message) {
        return DomainError.conflict("CrisisManagement.InvalidRecommendationOperation", message);
    }

    public static DomainError invalidRecommendationData(String message) {
        return DomainError.validation("CrisisManagement.InvalidRecommendationData", message);
    }
}
