package com.esri.core.geometry;

import org.proj4.PJException;

import java.util.Arrays;

/**
 * Created by davidraleigh on 5/12/17.
 */
public class Projecter {
    public static int transform(ProjectionTransformation projectionTransformation, Point[] pointsIn,
                         int count, Point[] pointsOut) throws org.proj4.PJException {
        double[] coordsIn = new double[pointsIn.length * 2];
        for (int i = 0; i < pointsIn.length; i++) {
            coordsIn[i * 2] = pointsIn[i].getX();
            coordsIn[i * 2 + 1] = pointsIn[i].getY();
        }

        double[] coordsOut = Projecter.transform(projectionTransformation, coordsIn, pointsIn.length);
        for (int i = 0; i < pointsOut.length; i++) {
            pointsOut[i].setX(coordsOut[i * 2]);
            pointsOut[i].setY(coordsOut[i * 2 + 1]);
        }

        return 0;
    }

    public static double[] transform(ProjectionTransformation projectionTransformation,
                              double[] coordsSrc, int pointCount) throws org.proj4.PJException {
        double[] pointsOut = Arrays.copyOf(coordsSrc, coordsSrc.length);
        projectionTransformation.getFromProj().transform(projectionTransformation.getToProj(), 2, pointsOut, 0, pointsOut.length / 2);
        return pointsOut;
    }

    static Geometry project(Geometry geometry,
                            ProjectionTransformation projectionTransformation,
                            ProgressTracker progressTracker) {
        Geometry result = null;
        try {
            switch (geometry.getType()) {
                case Unknown:
                    break;
                case Point:
                    result = projectPoint(geometry, projectionTransformation, progressTracker);
                    break;
                case Line:
                    break;
                case Envelope:
                    break;
                case MultiPoint:
                    break;
                case Polyline:
                    break;
                case Polygon:
                    break;
            }
        } catch (PJException e) {
            e.printStackTrace();
            return result;
        }

        return result;
    }

    static Geometry clipGeometry(Geometry geometry, ProjectionTransformation projectionTransformation, ProgressTracker progressTracker) {
        // TODO clip geometry by toTransformation Horizon

        return geometry;
    }

    static Geometry projectPoint(Geometry geometry,
                                 ProjectionTransformation projectionTransformation,
                                 ProgressTracker progressTracker) throws org.proj4.PJException {
        geometry = clipGeometry(geometry, projectionTransformation, progressTracker);
        Point outpoint = new Point();
        // TODO clean this idea up
        Point[] outputs = {outpoint};
        Point[] inputs = {(Point)geometry};
        transform(projectionTransformation, inputs, 1, outputs);
        return outputs[0];
    }

    static Geometry projectMultiPoint(Geometry geometry,
                                      ProjectionTransformation projectionTransformation,
                                      ProgressTracker progressTracker) {
        geometry = clipGeometry(geometry, projectionTransformation, progressTracker);

        return null;
    }
}
