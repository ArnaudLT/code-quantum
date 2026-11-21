package org.arnaudlt.or.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationConfigurationTest {
    @Test
    void parsesDefaultsAndOverrides() {
        ApplicationConfiguration defaults = ApplicationConfiguration.fromArgs(new String[0]);
        ApplicationConfiguration configured = ApplicationConfiguration.fromArgs(new String[]{
                "--input=problem.json", "--format=json-matrix", "--strategy=linearization",
                "--max-decimal-places=4", "--time-limit-seconds=5", "--workers=2", "--logs=true"
        });

        assertEquals("src/main/resources/qubo.json", defaults.input());
        assertEquals(4, configured.maxDecimalPlaces());
        assertEquals(5, configured.solverSettings().timeLimitSeconds());
        assertEquals(2, configured.solverSettings().workers());
        assertTrue(configured.solverSettings().logSearchProgress());
    }

    @Test
    void rejectsUnknownOptionsAndStrategies() {
        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfiguration.fromArgs(new String[]{"--unknown=value"}));
        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfiguration.fromArgs(new String[]{"--strategy=missing"}));
        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfiguration.fromArgs(new String[]{"--workers=0"}));
        assertThrows(IllegalArgumentException.class,
                () -> ApplicationConfiguration.fromArgs(new String[]{"--logs=perhaps"}));
    }
}