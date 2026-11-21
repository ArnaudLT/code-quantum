package org.arnaudlt.or.reading;

import org.arnaudlt.or.qubo.QuboProblem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonMatrixQuboReaderTest {
    private final JsonMatrixQuboReader reader = new JsonMatrixQuboReader();

    @Test
    void readsValidIntegerAndDecimalMatrices() throws Exception {
        QuboProblem integers = read("{\"qubo\":{\"matrix\":[[0,1],[1,-2]]}}");
        QuboProblem decimals = read("{\"qubo\":{\"matrix\":[[1.25,0.5],[0.5,-3.75]]}}");

        assertEquals(2, integers.size());
        assertEquals(-1.5, decimals.evaluate(new int[]{1, 1}));
    }

    @Test
    void rejectsMissingPropertiesAndInvalidMatrices() {
        assertInvalid("{}");
        assertInvalid("{\"qubo\":{}}");
        assertInvalid("{\"qubo\":{\"matrix\":[]}}");
        assertInvalid("{\"qubo\":{\"matrix\":[[1,2]]}}");
        assertInvalid("{\"qubo\":{\"matrix\":[[0,1],[2,0]]}}");
        assertInvalid("{\"qubo\":{\"matrix\":[[\"no\"]]}}");
        assertInvalid("{\"qubo\":{\"matrix\":[[null]]}}");
        assertInvalid("{\"qubo\":{\"matrix\":[[true]]}}");
        assertInvalid("{\"qubo\":{\"matrix\":[[1e400]]}}");
        assertInvalid("{\"qubo\":{\"matrix\":[[NaN]]}}");
        assertInvalid("{\"qubo\":{\"matrix\":[[-1]]},\"qubo\":{\"matrix\":[[0]]}}");
        assertInvalid("{\"qubo\":{\"matrix\":[[0]]}} {\"extra\":true}");
    }

    @Test
    void reportsMissingInputFile(@TempDir Path tempDir) {
        assertThrows(QuboReadException.class, () -> reader.read(tempDir.resolve("missing.json")));
    }

    private QuboProblem read(String json) throws QuboReadException {
        return reader.read(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }

    private void assertInvalid(String json) {
        assertThrows(QuboReadException.class, () -> read(json));
    }
}