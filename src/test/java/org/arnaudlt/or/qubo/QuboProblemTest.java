package org.arnaudlt.or.qubo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuboProblemTest {
    @Test
    void evaluatesPasqalMatrixConvention() {
        QuboProblem problem = new QuboProblem(new double[][]{{0, 1, 2}, {1, 0, 3}, {2, 3, 0}});

        assertEquals(3, problem.size());
        assertEquals(4.0, problem.evaluate(new int[]{1, 0, 1}));
        assertEquals(0.0, problem.evaluate(new int[]{0, 1, 0}));
    }

    @Test
    void evaluatesDiagonalCoefficientsAsLinearTerms() {
        QuboProblem problem = new QuboProblem(new double[][]{{-2, 0}, {0, 3}});

        assertEquals(1.0, problem.evaluate(new int[]{1, 1}));
    }

    @Test
    void rejectsInvalidDimensionsCoefficientsAndAsymmetry() {
        assertThrows(InvalidQuboException.class, () -> new QuboProblem(new double[0][]));
        assertThrows(InvalidQuboException.class, () -> new QuboProblem(new double[][]{{1, 2}}));
        assertThrows(InvalidQuboException.class, () -> new QuboProblem(new double[][]{{0, 1}, {2, 0}}));
        assertThrows(InvalidQuboException.class, () -> new QuboProblem(new double[][]{{Double.NaN}}));
    }

    @Test
    void rejectsNonBinaryOrWrongLengthSolutions() {
        QuboProblem problem = new QuboProblem(new double[][]{{1}});

        assertThrows(IllegalArgumentException.class, () -> problem.evaluate(new int[0]));
        assertThrows(IllegalArgumentException.class, () -> problem.evaluate(new int[]{2}));
    }

    @Test
    void protectsMatrixFromCallerMutation() {
        double[][] matrix = {{2}};
        QuboProblem problem = new QuboProblem(matrix);
        matrix[0][0] = 9;

        assertEquals(2.0, problem.coefficient(0, 0));
    }
}