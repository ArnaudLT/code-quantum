package org.arnaudlt.or.cpsat;

import com.google.ortools.sat.CpSolverStatus;
import org.arnaudlt.or.qubo.QuboProblem;
import org.arnaudlt.or.solving.QuboSolution;
import org.arnaudlt.or.solving.ResolutionStatus;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpSatQuboSolverTest {
    private final LinearizedQuboCpModelFactory factory = new LinearizedQuboCpModelFactory(
            new DecimalCoefficientScaling(6));
    private final CpSatQuboSolver solver = new CpSatQuboSolver(factory, new CpSatSettings(10, 1, false));

    @Test
    void solvesKnownProblemAndMatchesExhaustiveEnumeration() {
        QuboProblem problem = new QuboProblem(new double[][]{{-3, 1, 2}, {1, -2, 3}, {2, 3, -4}});
        QuboSolution result = solver.solve(problem);

        assertEquals(ResolutionStatus.OPTIMAL, result.status());
        assertArrayEquals(new int[]{0, 0, 1}, result.values());
        assertEquals(-4.0, result.objectiveValue());
        assertEquals(problem.evaluate(result.values()), result.objectiveValue());
        assertEquals(-4.0, exhaustiveMinimum(problem));
    }

    @Test
    void matchesDirectCostsForDecimalCoefficients() {
        QuboProblem problem = new QuboProblem(new double[][]{{-1.25, 0.3}, {0.3, -0.75}});
        QuboSolution result = solver.solve(problem);

        assertEquals(ResolutionStatus.OPTIMAL, result.status());
        assertEquals(problem.evaluate(result.values()), result.objectiveValue());
        assertEquals(exhaustiveMinimum(problem), result.objectiveValue());
    }

    @Test
    void matchesExhaustiveEnumerationForSeededIntegerAndDecimalProblems() {
        Random random = new Random(4815162342L);
        double[] coefficients = {-2, -1, -0.5, 0, 0.25, 1, 2};
        for (int sample = 0; sample < 12; sample++) {
            double[][] matrix = new double[3][3];
            for (int row = 0; row < matrix.length; row++) {
                matrix[row][row] = coefficients[random.nextInt(coefficients.length)];
                for (int column = row + 1; column < matrix.length; column++) {
                    matrix[row][column] = coefficients[random.nextInt(coefficients.length)];
                    matrix[column][row] = matrix[row][column];
                }
            }
            QuboProblem problem = new QuboProblem(matrix);
            QuboSolution result = solver.solve(problem);

            assertEquals(ResolutionStatus.OPTIMAL, result.status());
            assertEquals(exhaustiveMinimum(problem), result.objectiveValue(), 1e-9);
            assertEquals(problem.evaluate(result.values()), result.objectiveValue(), 1e-9);
        }
    }

    @Test
    void mapsEverySolverStatus() {
        assertEquals(ResolutionStatus.OPTIMAL, CpSatQuboSolver.mapStatus(CpSolverStatus.OPTIMAL));
        assertEquals(ResolutionStatus.FEASIBLE, CpSatQuboSolver.mapStatus(CpSolverStatus.FEASIBLE));
        assertEquals(ResolutionStatus.NO_SOLUTION, CpSatQuboSolver.mapStatus(CpSolverStatus.INFEASIBLE));
        assertEquals(ResolutionStatus.INTERRUPTED, CpSatQuboSolver.mapStatus(CpSolverStatus.UNKNOWN));
        assertThrows(IllegalStateException.class, () -> CpSatQuboSolver.mapStatus(CpSolverStatus.MODEL_INVALID));
    }

    @Test
    void returnsNoSolutionForContradictoryCpSatModel() {
        QuboCpModel model = factory.build(new QuboProblem(new double[][]{{1}}));
        model.model().addEquality(model.decisionVariables().getFirst(), 0);
        model.model().addEquality(model.decisionVariables().getFirst(), 1);

        QuboSolution result = solver.solve(model);

        assertEquals(ResolutionStatus.NO_SOLUTION, result.status());
        assertEquals(0, result.values().length);
        assertNull(result.objectiveValue());
    }

    private double exhaustiveMinimum(QuboProblem problem) {
        double minimum = Double.POSITIVE_INFINITY;
        for (int mask = 0; mask < (1 << problem.size()); mask++) {
            int[] values = new int[problem.size()];
            for (int index = 0; index < values.length; index++) {
                values[index] = (mask >> index) & 1;
            }
            minimum = Math.min(minimum, problem.evaluate(values));
        }
        return minimum;
    }
}