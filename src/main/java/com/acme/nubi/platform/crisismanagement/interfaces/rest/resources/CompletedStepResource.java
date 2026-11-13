package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

import java.time.Instant;

public record CompletedStepResource(Long stepId, boolean skipped, Instant completedAt) {
}
