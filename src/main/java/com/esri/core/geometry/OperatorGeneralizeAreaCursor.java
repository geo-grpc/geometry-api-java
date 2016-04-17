package com.esri.core.geometry;

/**
 * Created by davidraleigh on 4/17/16.
 */
public class OperatorGeneralizeAreaCursor extends GeometryCursor {
    ProgressTracker m_progressTracker;
    GeometryCursor m_geoms;
    double m_maxDeviation;
    boolean m_bRemoveDegenerateParts;
    GeneralizeAreaType m_generalizeAreaType;

    public OperatorGeneralizeAreaCursor(GeometryCursor geoms,
                                        double maxDeviation,
                                        boolean bRemoveDegenerateParts,
                                        GeneralizeAreaType generalizeAreaType,
                                        ProgressTracker progressTracker) {
        m_geoms = geoms;
        m_maxDeviation = maxDeviation;
        m_progressTracker = progressTracker;
        m_bRemoveDegenerateParts = bRemoveDegenerateParts;
        m_generalizeAreaType = generalizeAreaType;
    }

    @Override
    public Geometry next() {
        // TODO Auto-generated method stub
        Geometry geom = m_geoms.next();
        if (geom == null)
            return null;
        return GeneralizeArea(geom);
    }

    @Override
    public int getGeometryID() {
        // TODO Auto-generated method stub
        return m_geoms.getGeometryID();
    }

    private Geometry GeneralizeArea(Geometry geom) {
        return null;
    }
}
