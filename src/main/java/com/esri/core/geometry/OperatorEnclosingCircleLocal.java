package com.esri.core.geometry;

public class OperatorEnclosingCircleLocal extends OperatorEnclosingCircle {
    @Override
    public GeometryCursor execute(GeometryCursor geoms, SpatialReference spatialReference, ProgressTracker progressTracker) {
        return new OperatorEnclosingCircleCursor(geoms, spatialReference, progressTracker);
    }

    @Override
    public Geometry execute(Geometry geom, SpatialReference spatialReference, ProgressTracker progressTracker) {
        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(geom), spatialReference, progressTracker);
        return operatorEnclosingCircleCursor.next();
    }
}
