package com.acme.nubi.platform.crisismanagement.domain.model.valueobjects;

public enum SensitivityLevel {
    LOW(0),
    MEDIUM(1),
    HIGH(2),
    VERY_HIGH(3);

    private final int rank;

    SensitivityLevel(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }
}
