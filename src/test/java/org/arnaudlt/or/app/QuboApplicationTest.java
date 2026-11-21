package org.arnaudlt.or.app;

import org.arnaudlt.or.solving.QuboSolution;
import org.arnaudlt.or.solving.ResolutionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuboApplicationTest {
    @Test
    void loadsExampleFromClasspathAndSolvesIt() throws Exception {
        ApplicationConfiguration configuration = ApplicationConfiguration.fromArgs(new String[]{
                "--input=qubo.json", "--time-limit-seconds=10"
        });

        QuboSolution result = new QuboApplication().execute(configuration);

        assertEquals(ResolutionStatus.OPTIMAL, result.status());
        assertEquals(-4.0, result.objectiveValue());
    }

    @Test
    void reportsMissingFile() {
        ApplicationConfiguration configuration = ApplicationConfiguration.fromArgs(new String[]{
                "--input=missing-application-input.json"
        });

        assertThrows(Exception.class, () -> new QuboApplication().execute(configuration));
    }
}