package com.acme.nubi.platform.supportnetwork.application.internal.outboundservices.acl;

import java.time.LocalDateTime;

public record AlertData(
        Long id,
        Long profileId,
        String type,
        String message,
        String severity,
        LocalDateTime createdAt) {
}
