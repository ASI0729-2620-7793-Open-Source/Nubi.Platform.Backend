package com.acme.nubi.platform.crisismanagement.domain.model.valueobjects;

public enum SosSessionStatus {
    IN_PROGRESS,
    FINISHED,
    FINISHED_EARLY;

    public boolean isOpen() {
        return this == IN_PROGRESS;
    }
}
