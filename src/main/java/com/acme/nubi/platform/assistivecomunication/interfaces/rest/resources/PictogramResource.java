package com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources;

public record PictogramResource(Long id, Long categoryId, String label, String phrase, String imageUrl,
                                String audioUrl, boolean hasAudio, boolean favorite, boolean custom,
                                Integer displayOrder) {
}
