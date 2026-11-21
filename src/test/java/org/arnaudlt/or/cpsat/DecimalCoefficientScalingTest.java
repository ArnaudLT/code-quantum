package org.arnaudlt.or.cpsat;

import org.arnaudlt.or.qubo.QuboProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DecimalCoefficientScalingTest {
    @Test
    void convertsIntegerAndDecimalObjectivesExactly() {
        IntegerQuboObjective integers = new DecimalCoefficientScaling(0)
                .convert(new QuboProblem(new double[][]{{-3, 2}, {2, 4}}));
        IntegerQuboObjective decimals = new DecimalCoefficientScaling(2)
                .convert(new QuboProblem(new double[][]{{1.25, 0.3}, {0.3, -2}}));

        assertEquals(0, integers.decimalPlaces());
        assertArrayEquals(new long[]{-3, 4}, integers.linear());
        assertEquals(4, integers.quadraticTerms().getFirst().coefficient());
        assertEquals(2, decimals.decimalPlaces());
        assertArrayEquals(new long[]{125, -200}, decimals.linear());
        assertEquals(60, decimals.quadraticTerms().getFirst().coefficient());
    }

    @Test
    void omitsZeroQuadraticTermsAndRejectsPrecisionOrRangeLoss() {
        IntegerQuboObjective zero = new DecimalCoefficientScaling(0)
                .convert(new QuboProblem(new double[][]{{0, 0}, {0, 0}}));

        assertEquals(0, zero.quadraticTerms().size());
        assertThrows(CoefficientConversionException.class,
                () -> new DecimalCoefficientScaling(2).convert(new QuboProblem(new double[][]{{0.001}})));
        assertThrows(CoefficientConversionException.class,
                () -> new DecimalCoefficientScaling(0).convert(new QuboProblem(new double[][]{{1e17}})));
    }
}