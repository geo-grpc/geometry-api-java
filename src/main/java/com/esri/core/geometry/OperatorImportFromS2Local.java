package com.esri.core.geometry;

import java.util.ArrayDeque;


public class OperatorImportFromS2Local extends OperatorImportFromS2 {
    @Override
    public GeometryCursor execute(ArrayDeque<Integer> s2ids, ProgressTracker progressTracker) {
        return new OperatorImportFromS2Cursor(s2ids);
    }

    @Override
    public Geometry execute(int s2id, ProgressTracker progressTracker) {
        return OperatorImportFromS2Cursor.createS2Geometry(s2id);
    }
}
