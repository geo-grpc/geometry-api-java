package com.esri.core.geometry;

public class OperatorImportFromMGRSLocal extends OperatorImportFromMGRS {
    @Override
    public GeometryCursor execute(SimpleStringCursor stringCursor, ProgressTracker progress_tracker) {
        return new OperatorImportFromMGRSCursor(stringCursor);
    }

    @Override
    public Geometry execute(Geometry.Type type, String string, ProgressTracker progress_tracker) {
        return MGRS.parseMGRS(string, false);
    }
}
