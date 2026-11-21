package org.arnaudlt.or.app;

import org.arnaudlt.or.cpsat.CpSatSettings;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record ApplicationConfiguration(String input, InputFormat format, ModelStrategy strategy,
                                       int maxDecimalPlaces, CpSatSettings solverSettings) {
    public static ApplicationConfiguration fromArgs(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (String argument : args) {
            if (!argument.startsWith("--") || !argument.contains("=")) {
                throw new IllegalArgumentException("Arguments must use --name=value syntax: " + argument);
            }
            int separator = argument.indexOf('=');
            String key = argument.substring(2, separator);
            if (options.putIfAbsent(key, argument.substring(separator + 1)) != null) {
                throw new IllegalArgumentException("Option was provided more than once: --" + key);
            }
        }
        for (String key : options.keySet()) {
            if (!java.util.Set.of("input", "format", "strategy", "max-decimal-places",
                    "time-limit-seconds", "workers", "logs").contains(key)) {
                throw new IllegalArgumentException("Unknown option: --" + key);
            }
        }

        String input = options.getOrDefault("input", "src/main/resources/qubo.json");
        InputFormat format = InputFormat.fromId(options.getOrDefault("format", "json-matrix"));
        ModelStrategy strategy = ModelStrategy.fromId(options.getOrDefault("strategy", "linearization"));
        int maxDecimals = parseInt(options, "max-decimal-places", 9);
        double timeLimit = parseDouble(options, "time-limit-seconds", 60.0);
        int workers = parseInt(options, "workers", 1);
        boolean logs = parseBoolean(options, "logs", false);
        if (maxDecimals < 0) {
            throw new IllegalArgumentException("--max-decimal-places must not be negative");
        }
        return new ApplicationConfiguration(input, format, strategy, maxDecimals,
                new CpSatSettings(timeLimit, workers, logs));
    }

    private static int parseInt(Map<String, String> options, String name, int defaultValue) {
        try {
            return options.containsKey(name) ? Integer.parseInt(options.get(name)) : defaultValue;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("--" + name + " must be an integer", exception);
        }
    }

    private static double parseDouble(Map<String, String> options, String name, double defaultValue) {
        try {
            return options.containsKey(name) ? Double.parseDouble(options.get(name)) : defaultValue;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("--" + name + " must be a number", exception);
        }
    }

    private static boolean parseBoolean(Map<String, String> options, String name, boolean defaultValue) {
        if (!options.containsKey(name)) {
            return defaultValue;
        }
        String value = options.get(name);
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("--" + name + " must be true or false");
        }
        return Boolean.parseBoolean(value);
    }

    public Path inputPath() {
        return Path.of(input);
    }
}