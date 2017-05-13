package com.esri.core.geometry;

/**
 * Created by davidraleigh on 5/10/17.
 */

import java.util.Random;

public class RandomPointMaker {

    /**
     * This assumes an equal area projection
     * @param geometry
     * @param pointsPerSquareKm
     * @param sr
     * @param progressTracker
     * @return
     */
    static Geometry generate(Geometry geometry,
                             double pointsPerSquareKm,
                             Random numberGenerator,
                             SpatialReference sr,
                             ProgressTracker progressTracker) {
        if (geometry.getType() != Geometry.Type.Polygon)
            return null;

        Polygon polygon = (Polygon)geometry;
        // TODO should iterate over paths
        // TODO iterator should check for containment. If a path is contained within another, random points shouldn't
        // be generated for that contained path
        // Ask Aaron if paths are written to attribute stream such that paths contained come after container paths
        return __makeRandomPoints(polygon, pointsPerSquareKm, numberGenerator, sr, progressTracker);
    }

    // TODO input should be multiplath
    static Geometry __makeRandomPoints(Polygon polygon,
                                       double pointsPerSquareKm,
                                       Random numberGenerator,
                                       SpatialReference sr,
                                       ProgressTracker progressTracker) {
        Envelope inputEnvelope = new Envelope();
        polygon.queryEnvelope(inputEnvelope);

        // TODO, get center lat lon from envelope
            // TODO If Spatial Reference is PCS Project Envelope to GCS

        // From GCS Grab point
        double longitude = 0;
        double latitude = inputEnvelope.getCenterY();

        // TODO Generate Azimuthal Equal Area Projection from lat lon
        // +proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs
        String proj4 = String.format(
                "+proj=laea +lat_0=52 +lon_0=10 +x_0=%f +y_0=%f +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
                longitude, latitude);

        // TODO get ellipsoid model (right now assumes everything is GRS80


        // TODO Projection Transformation from azi and input sr

        // TODO Project bounding coordinates to equal area

        // Calculate point count within bounding box by dividing area in meters
        // by 1000

        // TODO replace this envelope with above equal area envelop
        Envelope envelope = inputEnvelope;
        double areaKm = envelope.calculateArea2D() / 1000.0;

        double xmin = envelope.getXMin();
        double xmax = envelope.getXMax();
        double ymin = envelope.getYMin();
        double ymax = envelope.getYMax();

        int pointCount = (int) Math.ceil(areaKm * pointsPerSquareKm);

        double[] xy = new double[pointCount * 2];

        double val = 0.0;
        double xdiff = xmax - xmin;
        double ydiff = ymax - ymin;
        for (int i = 0; i < pointCount * 2; i++) {
            if (i % 2 == 0) // x val
                val = numberGenerator.nextDouble() * xdiff + xmin;
            else            // y val
                val = numberGenerator.nextDouble() * ydiff + ymin;

            xy[i] = val;
        }

        // Create Multipoint from vertices
        MultiPoint multiPoint = new MultiPoint();
        multiPoint.add(4, 4);
        MultiVertexGeometryImpl multiVertexGeometry = (MultiVertexGeometryImpl)multiPoint._getImpl();
        AttributeStreamOfDbl attributeStreamOfDbl = new AttributeStreamOfDbl(pointCount * 2);
        attributeStreamOfDbl.writeRange(0, pointCount * 2, xy, 0, true);
        multiVertexGeometry.setAttributeStreamRef(0, attributeStreamOfDbl);
        //multiVertexGeometry._resizeImpl(pointCount);
        multiPoint.resize(pointCount);

        // TODO project multipoint back to input spatial reference (it is necessary to do it here,
        // because if we projected the above array, then we wouldn't benefit from clipping

        // Intersect by input geometry
        return GeometryEngine.intersect(multiPoint, polygon, sr);
    }
}
