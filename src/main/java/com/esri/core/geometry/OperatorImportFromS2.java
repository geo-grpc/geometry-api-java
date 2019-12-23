package com.esri.core.geometry;

import java.util.ArrayDeque;

public abstract class OperatorImportFromS2 extends Operator {
    @Override
    public Type getType() {
        return Type.ImportFromS2;
    }

    public abstract GeometryCursor execute(ArrayDeque<Long> s2ids, double maxDeviation, ProgressTracker progressTracker);

    public abstract Geometry execute(long s2id, double maxDeviation, ProgressTracker progressTracker);

    public static OperatorImportFromS2 local() {
        return (OperatorImportFromS2) OperatorFactoryLocal.getInstance().getOperator(Type.ImportFromS2);
    }
}
