package com.esri.core.geometry;

public class OperatorEnclosingCircleCursor extends GeometryCursor {
    SpatialReference m_spatialReference;
    ProgressTracker m_progressTracker;

    public OperatorEnclosingCircleCursor(GeometryCursor geoms, SpatialReference spatialReference, ProgressTracker progressTracker) {
        m_inputGeoms = geoms;
        m_spatialReference = spatialReference;
        m_progressTracker = progressTracker;
    }

    @Override
    public Geometry next() {
        if (hasNext())
            return postProject(getCircle(preProjectNext()));

        return null;
    }

    protected Geometry getCircle(Geometry geometry) {
        if (geometry.getType() == Geometry.Type.Point || ((MultiVertexGeometry)geometry).getPointCount() <= 1)
            return geometry;

        EnclosingCircler enclosingCircler = new EnclosingCircler(geometry, m_spatialReference, m_progressTracker);
        return enclosingCircler.search();
    }


}
