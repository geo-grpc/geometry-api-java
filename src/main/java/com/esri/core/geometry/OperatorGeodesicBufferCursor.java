package com.esri.core.geometry;

/**
 * Created by davidraleigh on 2/20/16.
 */
public class OperatorGeodesicBufferCursor extends GeometryCursor {

    private GeometryCursor m_inputGeoms;
    private SpatialReferenceImpl m_spatialReference;
    private ProgressTracker m_progressTracker;
    private double[] m_distances;
    private double m_maxDeviation;
    private Envelope2D m_currentUnionEnvelope2D;
    private boolean m_bUnion;

    private int m_index;
    private int m_dindex;

    // GeometryCursor inputGeometries, SpatialReference sr, int curveType, double[] distancesMeters, double maxDeviationMeters, boolean bReserved, boolean bUnion, ProgressTracker progressTracker
    OperatorGeodesicBufferCursor(GeometryCursor inputGeoms,
                                 SpatialReference sr,
                                 double[] distances,
                                 double maxDeviation,
                                 boolean bReserved,
                                 boolean b_union,
                                 ProgressTracker progressTracker) {

        if (sr.getID() != 4326) {
            throw new GeometryException("GeodesicBuffer only implemented for 4326, WGS84");
        }
        m_index = -1;
        m_inputGeoms = inputGeoms;
        m_spatialReference = (SpatialReferenceImpl) (sr);
        m_distances = distances;
        m_maxDeviation = maxDeviation;
        m_bUnion = b_union;
        m_currentUnionEnvelope2D = new Envelope2D();
        m_currentUnionEnvelope2D.setEmpty();
        m_dindex = -1;
        m_progressTracker = progressTracker;
    }

    @Override
    public Geometry next() {
        Geometry geom;
        while ((geom = m_inputGeoms.next()) != null) {
            m_index = m_inputGeoms.getGeometryID();
            if (m_dindex + 1 < m_distances.length)
                m_dindex++;

            return geodesicBuffer(geom, m_distances[m_dindex]);
        }
        return null;
    }

    @Override
    public int getGeometryID() {
        return m_index;
    }

    // virtual bool IsRecycling() OVERRIDE { return false; }
    Geometry geodesicBuffer(Geometry geom, double distance) {
        return GeodesicBufferer.buffer(geom, distance, m_spatialReference, m_maxDeviation, 96, m_progressTracker);
    }
}
