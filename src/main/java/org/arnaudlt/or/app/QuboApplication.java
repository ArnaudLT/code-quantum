package org.arnaudlt.or.app;

import org.arnaudlt.or.cpsat.CpSatQuboSolver;
import org.arnaudlt.or.qubo.QuboProblem;
import org.arnaudlt.or.reading.QuboReadException;
import org.arnaudlt.or.reading.QuboReader;
import org.arnaudlt.or.solving.QuboSolution;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class QuboApplication {

    public QuboSolution execute(ApplicationConfiguration configuration) throws QuboReadException {
        QuboReader reader = configuration.format().createReader();
        QuboProblem problem = loadProblem(reader, configuration.input());
        CpSatQuboSolver solver = new CpSatQuboSolver(
                configuration.strategy().createFactory(configuration.maxDecimalPlaces()),
                configuration.solverSettings());
        return solver.solve(problem);
    }

    private QuboProblem loadProblem(QuboReader reader, String input) throws QuboReadException {
        Path path = Path.of(input);
        if (Files.isRegularFile(path)) {
            return reader.read(path);
        }
        String resourceName = input.startsWith("/") ? input.substring(1) : input;
        String resourcesPrefix = "src/main/resources/";
        if (resourceName.startsWith(resourcesPrefix)) {
            resourceName = resourceName.substring(resourcesPrefix.length());
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream resource = classLoader.getResourceAsStream(resourceName)) {
            if (resource == null) {
                throw new QuboReadException("Input file was not found on disk or classpath: " + input);
            }
            return reader.read(resource);
        } catch (java.io.IOException exception) {
            throw new QuboReadException("Could not close input resource: " + input, exception);
        }
    }
}