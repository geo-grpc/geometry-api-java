package com.esri.core.geometry;

/**
 * Created by davidraleigh on 2/21/16.
 */
public class OperatorGeodeticDensifyCursor extends GeometryCursor {
    GeometryCursor m_inputGeoms;
//    // SpatialReferenceImpl m_spatialReference;
    double m_maxLength;
    int m_index;
    Point m_startPoint;
    Point m_endPoint;

    public OperatorGeodeticDensifyCursor(GeometryCursor inputGeoms1,
                                         double maxLength,
                                         ProgressTracker progressTracker) {
        m_index = -1;
        m_inputGeoms = inputGeoms1;
        m_maxLength = maxLength;
        m_startPoint = new Point();
        m_endPoint = new Point();
    }

    @Override
    public int getGeometryID() {
        return m_index;
    }

    @Override
    public Geometry next() {
        Geometry geom;
        if ((geom = m_inputGeoms.next()) != null) {
            m_index = m_inputGeoms.getGeometryID();
            return densifyByLength(geom);
        }
        return null;
    }

    private Geometry densifyByLength(Geometry geom) {
        if (geom.isEmpty() || geom.getDimension() < 1)
            return geom;

        int geometryType = geom.getType().value();

        // TODO implement IsMultiPath and remove Polygon and Polyline call to
        // match Native
        // if (Geometry.IsMultiPath(geometryType))
        if (geometryType == Geometry.GeometryType.Polygon)
            return densifyMultiPath((MultiPath) geom);
        else if (Geometry.GeometryType.Polyline == geometryType)
            return densifyMultiPath((MultiPath) geom);
        else if (Geometry.isSegment(geometryType))
            return densifySegment((Segment) geom);
        else if (geometryType == Geometry.GeometryType.Envelope)
            return densifyEnvelope((Envelope) geom);
        else
            // TODO fix geometry exception to match native implementation
            throw GeometryException.GeometryInternalError();// GEOMTHROW(internal_error);
    }


    private double geodesicDistanceOnWGS84(Point2D startPt2D, Point2D endPt2D) {
        m_startPoint.setXY(startPt2D);
        m_endPoint.setXY(endPt2D);
        return SpatialReferenceImpl.geodesicDistanceOnWGS84Impl(m_startPoint, m_endPoint);
    }

    private double geodesicDistanceOnWGS84(Segment segment) {
        m_startPoint.setXY(segment.getStartXY());
        m_endPoint.setXY(segment.getEndXY());
        return SpatialReferenceImpl.geodesicDistanceOnWGS84Impl(m_startPoint, m_endPoint);
    }

//
    private Geometry densifySegment(Segment geom) {
        double length = geodesicDistanceOnWGS84(geom);
        if (length <= m_maxLength)
            return (Geometry) geom;

        Polyline polyline = new Polyline(geom.getDescription());
        polyline.addSegment(geom, true);
        return densifyMultiPath((MultiPath) polyline);
    }

    private Geometry densifyEnvelope(Envelope geom) {
        Polygon polygon = new Polygon(geom.getDescription());
        polygon.addEnvelope(geom, false);

        Envelope2D env2D = new Envelope2D();
        geom.queryEnvelope2D(env2D);
        double wTop = geodesicDistanceOnWGS84(env2D.getUpperLeft(), env2D.getUpperRight());
        double wBottom = geodesicDistanceOnWGS84(env2D.getLowerLeft(), env2D.getLowerRight());
        double height = geodesicDistanceOnWGS84(env2D.getUpperLeft(), env2D.getLowerLeft());// height on right is same as left. meridians are geodesics

        if (wTop <= m_maxLength && wBottom <= m_maxLength && height <= m_maxLength)
            return (Geometry)polygon;

        return densifyMultiPath((MultiPath) polygon);
    }

    private Geometry densifyMultiPath(MultiPath geom) {
        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984
        double rpu = Math.PI / 180.0;
        double dpu = 180.0 / Math.PI;
        PeDouble distanceMeters = new PeDouble();
        PeDouble az12= new PeDouble();
        PeDouble lam2 = new PeDouble();
        PeDouble phi2 = new PeDouble();


        MultiPath densifiedPoly = (MultiPath) geom.createInstance();
        SegmentIterator iter = geom.querySegmentIterator();
        while (iter.nextPath()) {
            boolean bStartNewPath = true;
            while (iter.hasNextSegment()) {
                Segment seg = iter.nextSegment();
                if (seg.getType().value() != Geometry.GeometryType.Line)
                    throw new GeometryException("curve densify not implemented");

                boolean bIsClosing = iter.isClosingSegment();

                // also get the segment's azimuth
                GeoDist.geodesic_distance_ngs(
                        a,
                        e2,
                        seg.getStartX() * rpu,
                        seg.getStartY() * rpu,
                        seg.getEndX() * rpu,
                        seg.getEndY() * rpu,
                        distanceMeters,
                        az12,
                        null);

                if (distanceMeters.val > m_maxLength) {// need to split
                    double dcount = Math.ceil(distanceMeters.val / m_maxLength);
                    double distInterval = distanceMeters.val / dcount;

                    Point point = new Point(geom.getDescription());// LOCALREFCLASS1(Point,
                    // VertexDescription,
                    // point,
                    // geom.getDescription());
                    if (bStartNewPath) {
                        bStartNewPath = false;
                        seg.queryStart(point);
                        densifiedPoly.startPath(point);
                    }

                    int n = (int)dcount - 1;
                    double distanceAlongGeodesic = 0.0;

                    for (int i = 0; i < n; i++) {
                        distanceAlongGeodesic += distInterval;
                        GeoDist.geodesic_forward(
                                a,
                                e2,
                                seg.getStartX() * rpu,
                                seg.getStartY() * rpu,
                                distanceAlongGeodesic,
                                az12.val,
                                lam2,
                                phi2);

                        densifiedPoly.lineTo(lam2.val * dpu, phi2.val * dpu);
                    }

                    if (!bIsClosing) {
                        seg.queryEnd(point);
                        densifiedPoly.lineTo(point);
                    } else {
                        densifiedPoly.closePathWithLine();
                    }

                    bStartNewPath = false;
                } else {
                    if (!bIsClosing)
                        densifiedPoly.addSegment(seg, bStartNewPath);
                    else
                        densifiedPoly.closePathWithLine();

                    bStartNewPath = false;
                }
            }
        }

        return (Geometry) densifiedPoly;
    }
}
