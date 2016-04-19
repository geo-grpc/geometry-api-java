package com.esri.core.geometry;

/**
 * Created by davidraleigh on 4/17/16.
 */
final public class OperatorGeneralizeAreaLocal extends OperatorGeneralizeArea {

    @Override
    public GeometryCursor execute(GeometryCursor geoms,
                                  double areaThreshold,
                                  boolean bRemoveDegenerateParts,
                                  GeneralizeAreaType generalizeAreaType,
                                  ProgressTracker progressTracker) {

        return new OperatorGeneralizeAreaCursor(geoms,
                                                areaThreshold,
                                                bRemoveDegenerateParts,
                                                generalizeAreaType,
                                                progressTracker);
    }

    @Override
    public Geometry execute(Geometry geom,
                            double areaThreshold,
                            boolean bRemoveDegenerateParts,
                            GeneralizeAreaType generalizeAreaType,
                            ProgressTracker progressTracker) {

        SimpleGeometryCursor inputGeomCurs = new SimpleGeometryCursor(geom);

        GeometryCursor geometryCursor = execute(inputGeomCurs,
                                                areaThreshold,
                                                bRemoveDegenerateParts,
                                                generalizeAreaType,
                                                progressTracker);

        return geometryCursor.next();
    }
}
