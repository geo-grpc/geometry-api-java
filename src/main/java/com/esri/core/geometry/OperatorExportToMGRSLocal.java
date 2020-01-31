package com.esri.core.geometry;

public class OperatorExportToMGRSLocal extends OperatorExportToMGRS {

    @Override
    public StringCursor execute(GeometryCursor geometryCursor, MGRS.Zoom zoom, ProgressTracker progress_tracker) {
        return new OperatorExportToMGRSCursor(geometryCursor, zoom, progress_tracker);
    }

    @Override
    public StringCursor execute(Geometry geometry, MGRS.Zoom zoom, ProgressTracker progress_tracker) {
        SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(geometry);
        return new OperatorExportToMGRSCursor(simpleGeometryCursor, zoom, progress_tracker);
    }
}
