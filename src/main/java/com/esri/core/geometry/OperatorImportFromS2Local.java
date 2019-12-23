package com.esri.core.geometry;

import java.util.ArrayDeque;


public class OperatorImportFromS2Local extends OperatorImportFromS2 {
    @Override
    public GeometryCursor execute(ArrayDeque<Long> s2ids, double maxDeviation, ProgressTracker progressTracker) {
        return new OperatorImportFromS2Cursor(s2ids, maxDeviation);
    }

    @Override
    public Geometry execute(long s2id, double maxDeviation, ProgressTracker progressTracker) {
        return OperatorImportFromS2Cursor.createS2Geometry(s2id, maxDeviation);
    }
}
