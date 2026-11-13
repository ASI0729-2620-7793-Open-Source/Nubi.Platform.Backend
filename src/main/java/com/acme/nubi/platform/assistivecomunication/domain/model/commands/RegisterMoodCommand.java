package com.acme.nubi.platform.assistivecomunication.domain.model.commands;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.EmotionalIntensityLevel;

public record RegisterMoodCommand(Long profileId, EmotionalIntensityLevel level) {
}
