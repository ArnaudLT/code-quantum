package org.arnaudlt.or.cpsat;

import com.google.ortools.Loader;
import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import org.arnaudlt.or.qubo.QuboProblem;

public final class LinearizedQuboCpModelFactory implements QuboCpModelFactory {
    private final DecimalCoefficientScaling coefficientScaling;

    public LinearizedQuboCpModelFactory(DecimalCoefficientScaling coefficientScaling) {
        this.coefficientScaling = coefficientScaling;
    }

    @Override
    public QuboCpModel build(QuboProblem problem) {
        Loader.loadNativeLibraries();
        IntegerQuboObjective objective = coefficientScaling.convert(problem);
        CpModel model = new CpModel();
        BoolVar[] decisions = new BoolVar[problem.size()];
        for (int index = 0; index < decisions.length; index++) {
            decisions[index] = model.newBoolVar("x_" + index);
        }

        LinearExprBuilder expression = LinearExpr.newBuilder();
        long[] linear = objective.linear();
        for (int index = 0; index < linear.length; index++) {
            if (linear[index] != 0) {
                expression.addTerm(decisions[index], linear[index]);
            }
        }
        for (IntegerQuboObjective.QuadraticTerm term : objective.quadraticTerms()) {
            BoolVar product = model.newBoolVar("y_" + term.firstIndex() + "_" + term.secondIndex());
            BoolVar first = decisions[term.firstIndex()];
            BoolVar second = decisions[term.secondIndex()];
            model.addLessOrEqual(product, first);
            model.addLessOrEqual(product, second);
            LinearExprBuilder lowerBound = LinearExpr.newBuilder();
            lowerBound.addTerm(product, 1);
            lowerBound.addTerm(first, -1);
            lowerBound.addTerm(second, -1);
            model.addGreaterOrEqual(lowerBound.build(), -1);
            expression.addTerm(product, term.coefficient());
        }
        model.minimize(expression.build());
        return new QuboCpModel(model, java.util.List.of(decisions), objective.decimalPlaces());
    }
}