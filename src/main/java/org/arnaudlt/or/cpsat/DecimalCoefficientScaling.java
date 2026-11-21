package org.arnaudlt.or.cpsat;

import org.arnaudlt.or.qubo.QuboProblem;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class DecimalCoefficientScaling {
    private static final BigInteger MAX_EXACT_DOUBLE_INTEGER = BigInteger.ONE.shiftLeft(53);
    private final int maxDecimalPlaces;

    public DecimalCoefficientScaling(int maxDecimalPlaces) {
        if (maxDecimalPlaces < 0) {
            throw new IllegalArgumentException("Maximum decimal places must not be negative");
        }
        this.maxDecimalPlaces = maxDecimalPlaces;
    }

    public IntegerQuboObjective convert(QuboProblem problem) {
        int decimalPlaces = 0;
        for (int row = 0; row < problem.size(); row++) {
            BigDecimal diagonal = BigDecimal.valueOf(problem.coefficient(row, row));
            decimalPlaces = Math.max(decimalPlaces, decimalPlaces(diagonal));
            for (int column = row + 1; column < problem.size(); column++) {
                BigDecimal quadratic = BigDecimal.valueOf(problem.coefficient(row, column)).multiply(BigDecimal.valueOf(2));
                decimalPlaces = Math.max(decimalPlaces, decimalPlaces(quadratic));
            }
        }
        if (decimalPlaces > maxDecimalPlaces) {
            throw new CoefficientConversionException("Exact conversion requires " + decimalPlaces
                    + " decimal places, exceeding configured maximum " + maxDecimalPlaces);
        }

        long[] linear = new long[problem.size()];
        List<IntegerQuboObjective.QuadraticTerm> quadraticTerms = new ArrayList<>();
        BigInteger absoluteCoefficientSum = BigInteger.ZERO;
        for (int row = 0; row < problem.size(); row++) {
            BigDecimal diagonal = BigDecimal.valueOf(problem.coefficient(row, row));
            long scaledDiagonal = scaleExactly(diagonal, decimalPlaces);
            linear[row] = scaledDiagonal;
            absoluteCoefficientSum = absoluteCoefficientSum.add(BigInteger.valueOf(scaledDiagonal).abs());

            for (int column = row + 1; column < problem.size(); column++) {
                BigDecimal coefficient = BigDecimal.valueOf(problem.coefficient(row, column))
                        .multiply(BigDecimal.valueOf(2));
                if (coefficient.signum() == 0) {
                    continue;
                }
                long scaledCoefficient = scaleExactly(coefficient, decimalPlaces);
                quadraticTerms.add(new IntegerQuboObjective.QuadraticTerm(row, column, scaledCoefficient));
                absoluteCoefficientSum = absoluteCoefficientSum.add(BigInteger.valueOf(scaledCoefficient).abs());
            }
        }
        if (absoluteCoefficientSum.compareTo(MAX_EXACT_DOUBLE_INTEGER) > 0) {
            throw new CoefficientConversionException("Absolute objective coefficient sum exceeds 2^53; "
                    + "CP-SAT's double-valued objective cannot be represented exactly");
        }
        return new IntegerQuboObjective(linear, quadraticTerms, decimalPlaces);
    }

    private int decimalPlaces(BigDecimal value) {
        return Math.max(0, value.stripTrailingZeros().scale());
    }

    private long scaleExactly(BigDecimal value, int decimalPlaces) {
        try {
            return value.movePointRight(decimalPlaces).longValueExact();
        } catch (ArithmeticException exception) {
            throw new CoefficientConversionException("Coefficient cannot be represented exactly as a long: " + value);
        }
    }
}