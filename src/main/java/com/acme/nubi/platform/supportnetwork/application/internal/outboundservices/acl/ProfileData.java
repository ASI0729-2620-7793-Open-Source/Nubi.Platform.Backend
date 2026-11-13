package com.acme.nubi.platform.supportnetwork.application.internal.outboundservices.acl;

public record ProfileData(
        Long id,
        String name,
        String avatarUrl,
        String currentEmotionalState) {
}
