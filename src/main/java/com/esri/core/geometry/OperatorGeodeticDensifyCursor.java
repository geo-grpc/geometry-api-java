package com.esri.core.geometry;

/**
 * Created by davidraleigh on 2/21/16.
 */
public class OperatorGeodeticDensifyCursor extends GeometryCursor {
    SpatialReferenceImpl m_spatialReference;
    double m_maxLength;
    Point m_startPoint;
    Point m_endPoint;
    ProgressTracker m_progressTracker;

    public OperatorGeodeticDensifyCursor(GeometryCursor inputGeoms1,
                                         SpatialReference spatialReference,
                                         double maxLength,
                                         ProgressTracker progressTracker) {
        m_inputGeoms = inputGeoms1;
        m_maxLength = maxLength;
        // TODO requires proper projection handing.
        m_spatialReference = (SpatialReferenceImpl) spatialReference;
        m_startPoint = new Point();
        m_endPoint = new Point();
        m_progressTracker = progressTracker;
    }

    @Override
    public Geometry next() {
        if (hasNext())
            return GeodesicDensifier.densifyByLength(m_inputGeoms.next(), m_spatialReference, m_maxLength, m_progressTracker);

        return null;
    }
}
