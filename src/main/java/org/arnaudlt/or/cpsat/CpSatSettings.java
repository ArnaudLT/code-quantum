package org.arnaudlt.or.cpsat;

public record CpSatSettings(double timeLimitSeconds, int workers, boolean logSearchProgress) {
    public CpSatSettings {
        if (!Double.isFinite(timeLimitSeconds) || timeLimitSeconds <= 0) {
            throw new IllegalArgumentException("Time limit must be a positive finite number of seconds");
        }
        if (workers < 1) {
            throw new IllegalArgumentException("Worker count must be at least 1");
        }
    }
}