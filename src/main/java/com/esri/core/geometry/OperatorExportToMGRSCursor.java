package com.esri.core.geometry;

public class OperatorExportToMGRSCursor extends StringCursor {
    private GeometryCursor m_geometryCursor;
    private MGRS.Zoom m_zoom;
    private SimpleStateEnum simpleStateEnum = SimpleStateEnum.SIMPLE_UNKNOWN;
    private static SpatialReference wgs84 = SpatialReference.create(4326);


    public OperatorExportToMGRSCursor(GeometryCursor geometryCursor, MGRS.Zoom zoom, ProgressTracker progressTracker) {
        if (geometryCursor == null)
            throw new GeometryException("invalid argument");

        m_zoom = zoom;
        m_geometryCursor = geometryCursor;
        setOperateSR(wgs84);
    }

    @Override
    public String next() {
        if (hasNext()) {
            Envelope envelope = new Envelope();
            Geometry value = preProjectNext(m_geometryCursor);
            value.queryEnvelope(envelope);
            return MGRS.getMGRSCode(envelope.getCenterX(), envelope.getCenterY(), m_zoom);
        }
        return null;
    }

    @Override
    public long getID() {
        return m_geometryCursor.getGeometryID();
    }

    @Override
    public SimpleStateEnum getSimpleState() {
        return simpleStateEnum;
    }

    @Override
    public String getFeatureID() {
        return m_geometryCursor.getFeatureID();
    }

    @Override
    public boolean hasNext() {
        return m_geometryCursor.hasNext();
    }
}
