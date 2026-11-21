package org.arnaudlt.or.qubo;

public class InvalidQuboException extends IllegalArgumentException {
    public InvalidQuboException(String message) {
        super(message);
    }
}