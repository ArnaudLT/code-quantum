package org.arnaudlt.or;

import org.arnaudlt.or.app.ApplicationConfiguration;
import org.arnaudlt.or.app.QuboApplication;
import org.arnaudlt.or.reading.QuboReadException;
import org.arnaudlt.or.solving.QuboSolution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodeQuantum {

    static void main(String[] args) {

        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.fromArgs(args);
            QuboSolution solution = new QuboApplication().execute(configuration);
            log.info("Status: {}", solution.status());
            if (solution.objectiveValue() != null) {
                log.info("Solution: {}", java.util.Arrays.toString(solution.values()));
                log.info("Objective: {}", solution.objectiveValue());
            }
            log.info("Solver: {}", solution.resolutionInfo().solver());
            log.info("Elapsed seconds: {}", solution.resolutionInfo().wallTimeSeconds());
        } catch (IllegalArgumentException | QuboReadException | IllegalStateException exception) {
            log.error("Could not solve QUBO: {}", exception.getMessage(), exception);
            System.exit(1);
        }
    }
}
