package org.arnaudlt.or.solving;

import org.arnaudlt.or.qubo.QuboProblem;

public interface QuboSolver {
    QuboSolution solve(QuboProblem problem);
}