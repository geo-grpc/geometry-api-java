package com.esri.core.geometry;

public abstract class OperatorExportToMGRS extends Operator {
    @Override
    public Type getType() {
        return Type.ExportToMGRS;
    }

    public abstract StringCursor execute(GeometryCursor geometryCursor, MGRS.Zoom zoom, ProgressTracker progress_tracker);

    public abstract StringCursor execute(Geometry geometry, MGRS.Zoom zoom, ProgressTracker progress_tracker);

    public static OperatorExportToMGRS local() {
        return (OperatorExportToMGRS) OperatorFactoryLocal.getInstance().getOperator(Type.ExportToMGRS);
    }
}
