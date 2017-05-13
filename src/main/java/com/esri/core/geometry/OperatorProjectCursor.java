package com.esri.core.geometry;

/**
 * Created by davidraleigh on 5/12/17.
 */
public class OperatorProjectCursor extends GeometryCursor {

    private GeometryCursor m_inputGeoms;
    ProjectionTransformation m_projectionTransformation;
    ProgressTracker m_progressTracker;
    private int m_index;

    OperatorProjectCursor(
            GeometryCursor inputGeoms,
            ProjectionTransformation projectionTransformation,
            ProgressTracker progressTracker) {
        m_index = -1;
        m_inputGeoms = inputGeoms;
        m_projectionTransformation = projectionTransformation;
    }

    @Override
    public Geometry next() {
        Geometry geometry;
        while ((geometry = m_inputGeoms.next()) != null) {
            m_index = m_inputGeoms.getGeometryID();
            return Projecter.project(geometry, m_projectionTransformation, m_progressTracker);
        }
        return null;
    }

    @Override
    public int getGeometryID() {
        return m_index;
    }
}
