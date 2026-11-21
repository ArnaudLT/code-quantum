package org.arnaudlt.or.app;

import org.arnaudlt.or.reading.JsonMatrixQuboReader;
import org.arnaudlt.or.reading.QuboReader;

public enum InputFormat {
    JSON_MATRIX("json-matrix");

    private final String id;

    InputFormat(String id) {
        this.id = id;
    }

    public QuboReader createReader() {
        return switch (this) {
            case JSON_MATRIX -> new JsonMatrixQuboReader();
        };
    }

    public static InputFormat fromId(String id) {
        for (InputFormat format : values()) {
            if (format.id.equals(id)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown input format '" + id + "'; supported: json-matrix");
    }
}