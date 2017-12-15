package com.esri.core.geometry;

/**
 * Created by davidraleigh on 2/21/16.
 */
public class OperatorGeodeticDensifyCursor extends GeometryCursor {
    GeometryCursor m_inputGeoms;
    SpatialReferenceImpl m_spatialReference;
    double m_maxLength;
    int m_index;
    Point m_startPoint;
    Point m_endPoint;
    ProgressTracker m_progressTracker;

    public OperatorGeodeticDensifyCursor(GeometryCursor inputGeoms1,
                                         SpatialReference spatialReference,
                                         double maxLength,
                                         ProgressTracker progressTracker) {
        m_index = -1;
        m_inputGeoms = inputGeoms1;
        m_maxLength = maxLength;
        m_spatialReference = (SpatialReferenceImpl) spatialReference;
        m_startPoint = new Point();
        m_endPoint = new Point();
        m_progressTracker = progressTracker;
    }

    @Override
    public boolean hasNext() { return m_inputGeoms != null && m_inputGeoms.hasNext(); }

    @Override
    public int getGeometryID() {
        return m_index;
    }

    @Override
    public Geometry next() {
        Geometry geom;
        if ((geom = m_inputGeoms.next()) != null) {
            m_index = m_inputGeoms.getGeometryID();
            return GeodesicDensifier.densifyByLength(geom, m_spatialReference, m_maxLength, m_progressTracker);
        }
        return null;
    }
}
