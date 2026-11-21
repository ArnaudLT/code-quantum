package org.arnaudlt.or.qubo;

public final class QuboProblem {
    private final double[][] matrix;

    public QuboProblem(double[][] matrix) {
        if (matrix == null) {
            throw new InvalidQuboException("QUBO matrix must not be null");
        }
        if (matrix.length == 0) {
            throw new InvalidQuboException("QUBO matrix must not be empty");
        }

        this.matrix = new double[matrix.length][];
        for (int row = 0; row < matrix.length; row++) {
            if (matrix[row] == null || matrix[row].length != matrix.length) {
                throw new InvalidQuboException("QUBO matrix must be square; row " + row
                        + " has length " + (matrix[row] == null ? "null" : matrix[row].length));
            }
            this.matrix[row] = matrix[row].clone();
            for (int column = 0; column < matrix.length; column++) {
                if (!Double.isFinite(this.matrix[row][column])) {
                    throw new InvalidQuboException("Coefficient at [" + row + "][" + column
                            + "] must be finite");
                }
            }
        }

        for (int row = 0; row < matrix.length; row++) {
            for (int column = row + 1; column < matrix.length; column++) {
                if (this.matrix[row][column] != this.matrix[column][row]) {
                    throw new InvalidQuboException("QUBO matrix must be symmetric; coefficients at ["
                            + row + "][" + column + "] and [" + column + "][" + row + "] differ");
                }
            }
        }
    }

    public int size() {
        return matrix.length;
    }

    public double coefficient(int row, int column) {
        return matrix[row][column];
    }

    public double evaluate(int[] solution) {
        if (solution == null) {
            throw new IllegalArgumentException("Solution must not be null");
        }
        if (solution.length != size()) {
            throw new IllegalArgumentException("Solution must contain exactly " + size() + " values");
        }
        for (int index = 0; index < solution.length; index++) {
            if (solution[index] != 0 && solution[index] != 1) {
                throw new IllegalArgumentException("Solution value at index " + index + " must be 0 or 1");
            }
        }

        double cost = 0.0;
        for (int row = 0; row < size(); row++) {
            for (int column = 0; column < size(); column++) {
                cost += solution[row] * matrix[row][column] * solution[column];
            }
        }
        return cost;
    }
}