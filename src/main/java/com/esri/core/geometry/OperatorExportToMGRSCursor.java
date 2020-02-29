package com.esri.core.geometry;

import java.util.List;

public class OperatorExportToMGRSCursor extends StringCursor {
    private GeometryCursor m_geometryCursor;
    private MGRS.Zoom m_zoom;
    private SimpleStateEnum simpleStateEnum = SimpleStateEnum.SIMPLE_UNKNOWN;
    private static SpatialReference wgs84 = SpatialReference.create(4326);
    private MGRSEnvelopeCursor m_mgrsEnvelopeCursor = null;

    public OperatorExportToMGRSCursor(GeometryCursor geometryCursor, MGRS.Zoom zoom, ProgressTracker progressTracker) {
        if (geometryCursor == null)
            throw new GeometryException("invalid argument");

        m_zoom = zoom;
        m_geometryCursor = geometryCursor;
    }

    @Override
    public String next() {
        if (hasNext() || (m_mgrsEnvelopeCursor != null && m_mgrsEnvelopeCursor.hasNext())) {
            if (m_mgrsEnvelopeCursor == null) {
                m_mgrsEnvelopeCursor = new MGRSEnvelopeCursor(m_geometryCursor, m_zoom);
            }

            Point center = (Point)m_mgrsEnvelopeCursor.next();
            return MGRS.getMGRSCode(center.getX(), center.getY(), m_zoom);
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
        return m_geometryCursor.hasNext() || (m_mgrsEnvelopeCursor != null && m_mgrsEnvelopeCursor.hasNext());
    }

    protected class MGRSEnvelopeCursor extends GeometryCursor {
        private Geometry m_geometry;
        private MGRS.Zoom m_zoom;
        private Envelope m_currentEnvelope = new Envelope();
        private GeometryCursor m_geometryCursorPieces = null;
        private double m_xmin;
        private double m_xmax;
        private double m_ymin;
        private double m_ymax;
        private Polyline m_equator;



        public MGRSEnvelopeCursor(GeometryCursor geometryCursor, MGRS.Zoom zoom) {
            m_zoom = zoom;
            m_equator = new Polyline();
            m_equator.startPath(-180, 0);
            m_equator.lineTo(180, 0);
            m_geometryCursorPieces = new OperatorCutCursor(false, geometryCursor, m_equator, geometryCursor.getSR(), null);
            m_geometryCursorPieces.setOperateSR(SpatialReference.create(4326));

        }

        @Override
        public boolean hasNext() {
            return m_geometryCursorPieces.hasNext() || m_xmin < m_xmax && m_ymin < m_ymax;
        }

        @Override
        public Geometry next() {
            if (m_currentEnvelope.isEmpty()) {
                SpatialReference inputSr = m_geometryCursorPieces.getSR();
                m_geometry = m_geometryCursorPieces.next();
                SpatialReference utmZone = SpatialReference.createUTM(m_geometry);

                m_geometry.queryEnvelope(m_currentEnvelope);

                m_xmin = Math.floor(m_currentEnvelope.getXMin() / m_zoom.m_level) * m_zoom.m_level;
                m_ymin = Math.floor(m_currentEnvelope.getYMin() / m_zoom.m_level) * m_zoom.m_level;
                m_xmax = Math.ceil(m_currentEnvelope.getXMax() / m_zoom.m_level) * m_zoom.m_level;
                m_ymax = Math.ceil(m_currentEnvelope.getYMax() / m_zoom.m_level) * m_zoom.m_level;
            }

            for (double currentX = m_xmin; currentX < m_xmax; currentX += m_zoom.m_level) {
                for (double currentY = m_ymin; currentY < m_ymax; currentY += m_zoom.m_level) {
                    m_currentEnvelope.setCoords(currentX, currentY, currentX + m_zoom.m_level, currentY + m_zoom.m_level);
                    if (OperatorIntersects.local().execute(m_geometry, m_currentEnvelope, null, null)) {
                        return m_currentEnvelope.getCenter();
                    }
                }
            }

            return null;
        }
    }
}
