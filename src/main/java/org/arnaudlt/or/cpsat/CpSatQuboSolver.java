package org.arnaudlt.or.cpsat;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import org.arnaudlt.or.qubo.QuboProblem;
import org.arnaudlt.or.solving.QuboSolution;
import org.arnaudlt.or.solving.QuboSolver;
import org.arnaudlt.or.solving.ResolutionInfo;
import org.arnaudlt.or.solving.ResolutionStatus;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;

@Slf4j
public final class CpSatQuboSolver implements QuboSolver {
    private static final String SOLVER_NAME = "OR-Tools CP-SAT";
    private final QuboCpModelFactory modelFactory;
    private final CpSatSettings settings;

    public CpSatQuboSolver(QuboCpModelFactory modelFactory, CpSatSettings settings) {
        this.modelFactory = modelFactory;
        this.settings = settings;
    }

    @Override
    public QuboSolution solve(QuboProblem problem) {
        return solve(modelFactory.build(problem));
    }

    public QuboSolution solve(QuboCpModel quboModel) {
        Loader.loadNativeLibraries();
        CpSolver solver = new CpSolver();
        solver.getParameters()
                .setMaxTimeInSeconds(settings.timeLimitSeconds())
                .setNumSearchWorkers(settings.workers())
                .setLogSearchProgress(settings.logSearchProgress())
                .setLogToStdout(false);
        if (settings.logSearchProgress()) {
            solver.setLogCallback(message -> log.info("{}", message));
        }

        CpSolverStatus status = solver.solve(quboModel.model());
        if (status == CpSolverStatus.MODEL_INVALID) {
            throw new IllegalStateException("CP-SAT rejected the generated model: " + solver.responseStats());
        }
        ResolutionStatus resolutionStatus = mapStatus(status);
        double wallTime = solver.wallTime();
        if (resolutionStatus == ResolutionStatus.NO_SOLUTION || resolutionStatus == ResolutionStatus.INTERRUPTED) {
            return new QuboSolution(resolutionStatus, new int[0], null,
                    new ResolutionInfo(SOLVER_NAME, wallTime, null));
        }

        int[] values = Arrays.stream(quboModel.decisionVariables().toArray(com.google.ortools.sat.BoolVar[]::new))
                .mapToInt(variable -> solver.booleanValue(variable) ? 1 : 0)
                .toArray();
        double objectiveValue = unscale(solver.objectiveValue(), quboModel.decimalPlaces());
        double bestBound = unscale(solver.bestObjectiveBound(), quboModel.decimalPlaces());
        return new QuboSolution(resolutionStatus, values, objectiveValue,
                new ResolutionInfo(SOLVER_NAME, wallTime, bestBound));
    }

    static ResolutionStatus mapStatus(CpSolverStatus status) {
        return switch (status) {
            case OPTIMAL -> ResolutionStatus.OPTIMAL;
            case FEASIBLE -> ResolutionStatus.FEASIBLE;
            case INFEASIBLE -> ResolutionStatus.NO_SOLUTION;
            case UNKNOWN -> ResolutionStatus.INTERRUPTED;
            case MODEL_INVALID -> throw new IllegalStateException("Invalid CP-SAT model");
            case UNRECOGNIZED -> throw new IllegalStateException("CP-SAT returned an unrecognized status");
        };
    }

    private double unscale(double value, int decimalPlaces) {
        return BigDecimal.valueOf(value).movePointLeft(decimalPlaces).doubleValue();
    }
}