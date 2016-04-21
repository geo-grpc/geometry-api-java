package com.esri.core.geometry;

/**
 * Created by davidraleigh on 4/17/16.
 * Generalizes geometries using Visvalingam algorithm
 */
public abstract class OperatorGeneralizeByArea extends Operator {
    @Override
    public Operator.Type getType() {
        return Operator.Type.GeneralizeByArea;
    }

    /**
     * Performs the Generalize operation on a geometry set. Point and
     * multipoint geometries are left unchanged. An envelope is converted to a
     * polygon.
     *
     */
    public abstract GeometryCursor execute(GeometryCursor geoms,
                                           double percentReduction,
                                           boolean bRemoveDegenerateParts,
                                           GeneralizeType generalizeType,
                                           ProgressTracker progressTracker);

    /**
     * Performs the Generalize operation on a single geometry. Point and
     * multipoint geometries are left unchanged. An envelope is converted to a
     * polygon.
     */
    public abstract Geometry execute(Geometry geom,
                                     double percentReduction,
                                     boolean bRemoveDegenerateParts,
                                     GeneralizeType generalizeType,
                                     ProgressTracker progressTracker);

    public static OperatorGeneralizeByArea local() {
        return (OperatorGeneralizeByArea) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.GeneralizeByArea);
    }
}
