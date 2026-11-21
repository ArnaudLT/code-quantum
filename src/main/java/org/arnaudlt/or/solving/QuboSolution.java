package org.arnaudlt.or.solving;

import java.util.Objects;

public final class QuboSolution {
    private final ResolutionStatus status;
    private final int[] values;
    private final Double objectiveValue;
    private final ResolutionInfo resolutionInfo;

    public QuboSolution(ResolutionStatus status, int[] values, Double objectiveValue,
                        ResolutionInfo resolutionInfo) {
        this.status = Objects.requireNonNull(status);
        this.values = values.clone();
        this.objectiveValue = objectiveValue;
        this.resolutionInfo = Objects.requireNonNull(resolutionInfo);
    }

    public ResolutionStatus status() {
        return status;
    }

    public int[] values() {
        return values.clone();
    }

    public Double objectiveValue() {
        return objectiveValue;
    }

    public ResolutionInfo resolutionInfo() {
        return resolutionInfo;
    }
}