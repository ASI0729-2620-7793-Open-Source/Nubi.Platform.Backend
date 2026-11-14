package com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.EmotionalIntensityLevel;
import jakarta.validation.constraints.NotNull;

public record RegisterMoodResource(@NotNull EmotionalIntensityLevel level) {
}
