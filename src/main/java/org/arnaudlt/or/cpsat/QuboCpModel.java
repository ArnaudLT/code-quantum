package org.arnaudlt.or.cpsat;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;

import java.util.List;

public record QuboCpModel(CpModel model, List<BoolVar> decisionVariables, int decimalPlaces) {
    public QuboCpModel {
        decisionVariables = List.copyOf(decisionVariables);
    }
}