package com.esri.core.geometry;

/**
 * Created by davidraleigh on 4/17/16.
 * Generalizes geometries using Visvalingam algorithm
 */
public abstract class OperatorGeneralizeArea extends Operator {
    @Override
    public Operator.Type getType() {
        return Operator.Type.GeneralizeArea;
    }

    /**
     * Performs the Generalize operation on a geometry set. Point and
     * multipoint geometries are left unchanged. An envelope is converted to a
     * polygon.
     *
     */
    public abstract GeometryCursor execute(GeometryCursor geoms,
                                           double areaThreshold,
                                           boolean bRemoveDegenerateParts,
                                           GeneralizeAreaType generalizeAreaType,
                                           ProgressTracker progressTracker);

    /**
     * Performs the Generalize operation on a single geometry. Point and
     * multipoint geometries are left unchanged. An envelope is converted to a
     * polygon.
     */
    public abstract Geometry execute(Geometry geom,
                                     double areaThreshold,
                                     boolean bRemoveDegenerateParts,
                                     GeneralizeAreaType generalizeAreaType,
                                     ProgressTracker progressTracker);

    public static OperatorGeneralizeArea local() {
        return (OperatorGeneralizeArea) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.GeneralizeArea);
    }
}
