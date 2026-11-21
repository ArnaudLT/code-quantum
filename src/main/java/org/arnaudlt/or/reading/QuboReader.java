package org.arnaudlt.or.reading;

import org.arnaudlt.or.qubo.QuboProblem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface QuboReader {
    QuboProblem read(InputStream input) throws QuboReadException;

    default QuboProblem read(Path path) throws QuboReadException {
        if (path == null || !Files.isRegularFile(path)) {
            throw new QuboReadException("Input file does not exist or is not a regular file: " + path);
        }
        try (InputStream input = Files.newInputStream(path)) {
            return read(input);
        } catch (IOException exception) {
            throw new QuboReadException("Could not read input file: " + path, exception);
        }
    }
}