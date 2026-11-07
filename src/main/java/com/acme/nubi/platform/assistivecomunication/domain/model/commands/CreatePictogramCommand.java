package com.acme.nubi.platform.assistivecomunication.domain.model.commands;

public record CreatePictogramCommand(Long profileId, String label, String phrase, String imageUrl,
                                     String audioUrl, Long categoryId) {
}
