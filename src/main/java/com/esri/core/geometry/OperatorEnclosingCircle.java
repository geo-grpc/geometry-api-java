package com.esri.core.geometry;

public abstract class OperatorEnclosingCircle extends Operator {
    @Override
    public Type getType() {
        return Type.EnclosingCircle;
    }


    public abstract GeometryCursor execute(GeometryCursor geoms, SpatialReference spatialReference, ProgressTracker progressTracker);

    /**
     * Performs the Generalize operation on a single geometry. Point and
     * multipoint geometries are left unchanged. An envelope is converted to a
     * polygon.
     */
    public abstract Geometry execute(Geometry geom, SpatialReference spatialReference, ProgressTracker progressTracker);

    public static OperatorEnclosingCircle local() {
        return (OperatorEnclosingCircle) OperatorFactoryLocal.getInstance().getOperator(Type.EnclosingCircle);
    }
}
