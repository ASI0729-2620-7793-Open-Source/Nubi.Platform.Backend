package com.acme.nubi.platform.selfregulation.interfaces.rest.resources;

public record CalmTimerResource(int durationMinutes, int remainingSeconds, boolean running) {
}
