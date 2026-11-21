package org.arnaudlt.or.app;

import org.arnaudlt.or.cpsat.DecimalCoefficientScaling;
import org.arnaudlt.or.cpsat.LinearizedQuboCpModelFactory;
import org.arnaudlt.or.cpsat.QuboCpModelFactory;

public enum ModelStrategy {
    LINEARIZATION("linearization");

    private final String id;

    ModelStrategy(String id) {
        this.id = id;
    }

    public QuboCpModelFactory createFactory(int maxDecimalPlaces) {
        return switch (this) {
            case LINEARIZATION -> new LinearizedQuboCpModelFactory(
                    new DecimalCoefficientScaling(maxDecimalPlaces));
        };
    }

    public static ModelStrategy fromId(String id) {
        for (ModelStrategy strategy : values()) {
            if (strategy.id.equals(id)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown model strategy '" + id + "'; supported: linearization");
    }
}