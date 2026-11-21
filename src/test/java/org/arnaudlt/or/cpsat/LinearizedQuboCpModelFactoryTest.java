package org.arnaudlt.or.cpsat;

import org.arnaudlt.or.qubo.QuboProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinearizedQuboCpModelFactoryTest {
    @Test
    void createsOneAuxiliaryAndThreeConstraintsForEachNonzeroPair() {
        QuboCpModel result = new LinearizedQuboCpModelFactory(new DecimalCoefficientScaling(3))
                .build(new QuboProblem(new double[][]{{1, 2, 0}, {2, -1, 0.25}, {0, 0.25, 0}}));

        assertEquals(3, result.decisionVariables().size());
        assertEquals(5, result.model().model().getVariablesCount());
        assertEquals(6, result.model().model().getConstraintsCount());
        assertEquals(1, result.decimalPlaces());
    }

    @Test
    void createsNoAuxiliariesForDiagonalOnlyObjective() {
        QuboCpModel result = new LinearizedQuboCpModelFactory(new DecimalCoefficientScaling(0))
                .build(new QuboProblem(new double[][]{{-2, 0}, {0, 4}}));

        assertEquals(2, result.model().model().getVariablesCount());
        assertEquals(0, result.model().model().getConstraintsCount());
    }
}