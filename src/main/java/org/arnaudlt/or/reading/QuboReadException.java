package org.arnaudlt.or.reading;

public class QuboReadException extends Exception {
    public QuboReadException(String message) {
        super(message);
    }

    public QuboReadException(String message, Throwable cause) {
        super(message, cause);
    }
}