package com.esri.core.geometry;

/**
 * Created by davidraleigh on 4/17/16.
 */
final public class OperatorGeneralizeByAreaLocal extends OperatorGeneralizeByArea {

    @Override
    public GeometryCursor execute(GeometryCursor geoms,
                                  double percentReduction,
                                  boolean bRemoveDegenerateParts,
                                  GeneralizeType generalizeType,
                                  ProgressTracker progressTracker) {

        return new OperatorGeneralizeByAreaCursor(geoms,
                percentReduction,
                                                bRemoveDegenerateParts,
                generalizeType,
                                                progressTracker);
    }

    @Override
    public Geometry execute(Geometry geom,
                            double percentReduction,
                            boolean bRemoveDegenerateParts,
                            GeneralizeType generalizeType,
                            ProgressTracker progressTracker) {

        SimpleGeometryCursor inputGeomCurs = new SimpleGeometryCursor(geom);

        GeometryCursor geometryCursor = execute(inputGeomCurs, percentReduction,
                                                bRemoveDegenerateParts, generalizeType,
                                                progressTracker);

        return geometryCursor.next();
    }
}
