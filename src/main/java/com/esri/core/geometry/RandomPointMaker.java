package com.esri.core.geometry;

/**
 * Created by davidraleigh on 5/10/17.
 */

import org.proj4.PJException;

import java.util.Random;

class RandomPointMaker {

    /**
     * This assumes an equal area projection
     *
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
                             ProgressTracker progressTracker) throws PJException {
        if (geometry.getType() != Geometry.Type.Polygon && geometry.getType() != Geometry.Type.Envelope)
            throw new GeometryException("Geometry input must be of type Polygon or Envelope");

        if (sr == null || sr.isLocal())
            throw new GeometryException("Spatial reference must be defined and must have unit definition");

        Polygon polygon = null;
        if (geometry.getType() == Geometry.Type.Envelope) {
            polygon = new Polygon();
            polygon.addEnvelope((Envelope) geometry, false);
        } else {
            polygon = (Polygon) geometry;
        }

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
                                       ProgressTracker progressTracker) throws PJException {
        // TODO for each ring project

        Envelope2D inputEnvelope2D = new Envelope2D();
        polygon.queryEnvelope2D(inputEnvelope2D);

        // From GCS Grab point
        // TODO change to work with other GCS
        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984

        Point2D ptCenter = new Point2D();
        GeoDist.getEnvCenter(a, e2, inputEnvelope2D, ptCenter);
        double longitude = ptCenter.x;
        double latitude = ptCenter.y;

        // create projection transformation that goes from input to input's equal area azimuthal projection
        // +proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs
        String proj4 = String.format(
                "+proj=laea +lat_0=%f +lon_0=%f +x_0=0.0 +y_0=0.0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
                longitude, latitude);
        SpatialReference spatialReferenceAzi = SpatialReference.createFromProj4(proj4);
        ProjectionTransformation forwardProjectionTransformation = new ProjectionTransformation(sr, spatialReferenceAzi);

        // Project bounding coordinates to equal area
        // equalAreaEnvelopeGeom must be a geometry/polygon because projection of envelope will almost certainly
        // or skew geometry
        // TODO, maybe it would be computationally cheaper or more accurate to project input polygon instead of it's envelope
        Geometry equalAreaEnvelopeGeom = OperatorProject.local().execute(new Envelope(inputEnvelope2D), forwardProjectionTransformation, progressTracker);

        Envelope2D equalAreaEnvelope = new Envelope2D();
        // envelope of projected envelope
        equalAreaEnvelopeGeom.queryEnvelope2D(equalAreaEnvelope);

        double areaKm = equalAreaEnvelope.getArea() / 1000.0;
        int pointCount = (int) Math.ceil(areaKm * pointsPerSquareKm);
        //http://stackoverflow.com/questions/3038392/do-java-arrays-have-a-maximum-size
        if (pointCount * 2 > Integer.MAX_VALUE - 8)
            throw new GeometryException("Random Point count outside of available");
        // TODO if the area of the envelope is more than twice that of the initial polygon, maybe a raster creation
        // of random multipoints would be required...?

        double[] xy = new double[pointCount * 2];

        double val = 0.0;
        double xdiff = equalAreaEnvelope.xmax - equalAreaEnvelope.xmin;
        double ydiff = equalAreaEnvelope.ymax - equalAreaEnvelope.ymin;
        for (int i = 0; i < pointCount * 2; i++) {
            if (i % 2 == 0) // x val
                val = numberGenerator.nextDouble() * xdiff + equalAreaEnvelope.xmin;
            else            // y val
                val = numberGenerator.nextDouble() * ydiff + equalAreaEnvelope.ymin;

            xy[i] = val;
        }

        // Create Multipoint from vertices
        MultiPoint multiPoint = new MultiPoint();
        MultiVertexGeometryImpl multiVertexGeometry = (MultiVertexGeometryImpl) multiPoint._getImpl();
        AttributeStreamOfDbl attributeStreamOfDbl = new AttributeStreamOfDbl(pointCount * 2);

        // TODO it would be better if we could just std::move the array.
        attributeStreamOfDbl.writeRangeMove(xy);
        multiVertexGeometry.setAttributeStreamRef(0, attributeStreamOfDbl);
        //multiVertexGeometry._resizeImpl(pointCount);
        multiPoint.resize(pointCount);
        multiVertexGeometry._setDirtyFlag(DirtyFlags.dirtyVerifiedStreams | DirtyFlags.dirtyIntervals | DirtyFlags.isStrongSimple, true);

        ProjectionTransformation backProjectionTransformation = new ProjectionTransformation(spatialReferenceAzi, sr);
        // project inplace instead of projecting a copy using OperatorProject::execute
        Projecter.projectMultiPoint(multiPoint, backProjectionTransformation, progressTracker);


        // TODO project multipoint back to input spatial reference (it is necessary to do it here,
        // because if we projected the above array, then we wouldn't benefit from clipping

        // Intersect by input geometry
        return GeometryEngine.intersect(multiPoint, polygon, sr);
    }
}
