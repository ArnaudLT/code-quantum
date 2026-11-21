package org.arnaudlt.or.cpsat;

import org.arnaudlt.or.qubo.QuboProblem;

public interface QuboCpModelFactory {
    QuboCpModel build(QuboProblem problem);
}