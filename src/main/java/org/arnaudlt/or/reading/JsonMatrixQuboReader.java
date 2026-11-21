package org.arnaudlt.or.reading;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.arnaudlt.or.qubo.InvalidQuboException;
import org.arnaudlt.or.qubo.QuboProblem;

import java.io.IOException;
import java.io.InputStream;

public final class JsonMatrixQuboReader implements QuboReader {
    private final ObjectMapper objectMapper;

    public JsonMatrixQuboReader() {
        JsonFactory factory = JsonFactory.builder()
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .build();
        objectMapper = new ObjectMapper(factory).enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    }

    @Override
    public QuboProblem read(InputStream input) throws QuboReadException {
        if (input == null) {
            throw new QuboReadException("Input stream must not be null");
        }
        try {
            JsonNode root = objectMapper.readTree(input);
            if (root == null || !root.isObject()) {
                throw new QuboReadException("JSON document must contain an object with property 'qubo'");
            }
            JsonNode qubo = root.get("qubo");
            if (qubo == null || !qubo.isObject()) {
                throw new QuboReadException("Required object 'qubo' is missing");
            }
            JsonNode matrixNode = qubo.get("matrix");
            if (matrixNode == null) {
                throw new QuboReadException("Required property 'qubo.matrix' is missing");
            }
            if (!matrixNode.isArray() || matrixNode.isEmpty()) {
                throw new QuboReadException("'qubo.matrix' must be a non-empty array of arrays");
            }

            double[][] matrix = new double[matrixNode.size()][];
            for (int row = 0; row < matrixNode.size(); row++) {
                JsonNode rowNode = matrixNode.get(row);
                if (!rowNode.isArray()) {
                    throw new QuboReadException("'qubo.matrix[" + row + "]' must be an array");
                }
                matrix[row] = new double[rowNode.size()];
                for (int column = 0; column < rowNode.size(); column++) {
                    JsonNode coefficient = rowNode.get(column);
                    if (coefficient == null || !coefficient.isNumber()) {
                        throw new QuboReadException("Coefficient at 'qubo.matrix[" + row + "][" + column
                                + "]' must be a number");
                    }
                    double value = coefficient.doubleValue();
                    if (!Double.isFinite(value)) {
                        throw new QuboReadException("Coefficient at 'qubo.matrix[" + row + "][" + column
                                + "]' must be finite");
                    }
                    matrix[row][column] = value;
                }
            }
            try {
                return new QuboProblem(matrix);
            } catch (InvalidQuboException exception) {
                throw new QuboReadException("Invalid QUBO matrix: " + exception.getMessage(), exception);
            }
        } catch (IOException exception) {
            throw new QuboReadException("Input is not valid JSON", exception);
        }
    }
}