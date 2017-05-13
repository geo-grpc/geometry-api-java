package com.esri.core.geometry;

import org.proj4.PJException;

import java.util.Arrays;

/**
 * Created by davidraleigh on 5/12/17.
 */
public class Projecter {
    static {
        System.loadLibrary("proj");
    }

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
                    result = projectMultiPoint(geometry, projectionTransformation, progressTracker);
                    break;
                case Polyline:
                    result = projectPolyline(geometry, projectionTransformation, progressTracker);
                    break;
                case Polygon:
                    result = projectPolygon(geometry, projectionTransformation, progressTracker);
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
                                      ProgressTracker progressTracker) throws org.proj4.PJException {
        MultiPoint multiPoint = (MultiPoint)clipGeometry(geometry, projectionTransformation, progressTracker);

        int pointCount = multiPoint.getPointCount();
        MultiVertexGeometryImpl multiVertexGeometry = (MultiVertexGeometryImpl)multiPoint._getImpl();

        AttributeStreamOfDbl xyPositions =  (AttributeStreamOfDbl)multiVertexGeometry.getAttributeStreamRef(0);
        double[] output = transform(projectionTransformation, xyPositions.m_buffer, pointCount);
        AttributeStreamOfDbl attributeStreamOfDbl = new AttributeStreamOfDbl(pointCount * 2);
        attributeStreamOfDbl.writeRange(0, pointCount * 2, output, 0, true);

        MultiPoint multiPointOut = new MultiPoint(multiPoint.getDescription());
        MultiVertexGeometryImpl multiVertexGeometryOut = (MultiVertexGeometryImpl)multiPointOut._getImpl();

        multiVertexGeometryOut.setAttributeStreamRef(0, attributeStreamOfDbl);
        multiVertexGeometryOut._resizeImpl(pointCount);
        multiPointOut.resize(pointCount);

        return multiPointOut;
    }

    static Geometry projectPolyline(Geometry geometry,
                                      ProjectionTransformation projectionTransformation,
                                      ProgressTracker progressTracker) throws org.proj4.PJException {
        Polyline polyline = (Polyline)clipGeometry(geometry, projectionTransformation, progressTracker);

        int pointCount = polyline.getPointCount();
        MultiVertexGeometryImpl multiVertexGeometry = (MultiVertexGeometryImpl)polyline._getImpl();

        AttributeStreamOfDbl xyPositions =  (AttributeStreamOfDbl)multiVertexGeometry.getAttributeStreamRef(0);
        double[] output = transform(projectionTransformation, xyPositions.m_buffer, pointCount);
        AttributeStreamOfDbl attributeStreamOfDbl = new AttributeStreamOfDbl(pointCount * 2);
        attributeStreamOfDbl.writeRange(0, pointCount * 2, output, 0, true);

        Polyline polylineOut = new Polyline(polyline.getDescription());
        MultiVertexGeometryImpl multiVertexGeometryOut = (MultiVertexGeometryImpl)polylineOut._getImpl();

        multiVertexGeometryOut.setAttributeStreamRef(0, attributeStreamOfDbl);
        multiVertexGeometryOut._resizeImpl(pointCount);

        return polylineOut;
    }

    static Geometry projectPolygon(Geometry geometry,
                                    ProjectionTransformation projectionTransformation,
                                    ProgressTracker progressTracker) throws org.proj4.PJException {
        Polygon polygon = (Polygon)clipGeometry(geometry, projectionTransformation, progressTracker);

        int pointCount = polygon.getPointCount();
        MultiVertexGeometryImpl multiVertexGeometry = (MultiVertexGeometryImpl)polygon._getImpl();

        AttributeStreamOfDbl xyPositions =  (AttributeStreamOfDbl)multiVertexGeometry.getAttributeStreamRef(0);
        double[] output = transform(projectionTransformation, xyPositions.m_buffer, pointCount);
        AttributeStreamOfDbl attributeStreamOfDbl = new AttributeStreamOfDbl(pointCount * 2);
        attributeStreamOfDbl.writeRange(0, pointCount * 2, output, 0, true);

        Polygon polygonOut = new Polygon(polygon.getDescription());
        MultiVertexGeometryImpl multiVertexGeometryOut = (MultiVertexGeometryImpl)polygonOut._getImpl();

        multiVertexGeometryOut.setAttributeStreamRef(0, attributeStreamOfDbl);
        multiVertexGeometryOut._resizeImpl(pointCount);

        return polygonOut;
    }
}
