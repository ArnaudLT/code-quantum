package org.arnaudlt.or.cpsat;

import java.util.List;

public record IntegerQuboObjective(long[] linear, List<QuadraticTerm> quadraticTerms, int decimalPlaces) {
    public IntegerQuboObjective {
        linear = linear.clone();
        quadraticTerms = List.copyOf(quadraticTerms);
    }

    @Override
    public long[] linear() {
        return linear.clone();
    }

    public record QuadraticTerm(int firstIndex, int secondIndex, long coefficient) {
    }
}