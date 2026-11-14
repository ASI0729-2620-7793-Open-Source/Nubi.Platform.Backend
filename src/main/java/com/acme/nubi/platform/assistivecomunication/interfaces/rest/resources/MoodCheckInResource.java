package com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources;

import java.time.LocalDateTime;

public record MoodCheckInResource(Long id, String level, LocalDateTime checkedAt) {
}
