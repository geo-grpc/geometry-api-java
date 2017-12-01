package com.esri.core.geometry;

public class OperatorExportToWktCursor extends StringCursor {
    GeometryCursor m_geometryCursor;
    int m_export_flags;
    int m_index;

    public OperatorExportToWktCursor(int exportFlags, GeometryCursor geometryCursor, ProgressTracker progressTracker) {
        m_index = -1;
        if (geometryCursor == null)
            throw new GeometryException("invalid argument");

        m_export_flags = exportFlags;
        m_geometryCursor = geometryCursor;
    }

    @Override
    public String next() {
        Geometry geometry = m_geometryCursor.next();
        if (geometry != null) {
            m_index = m_geometryCursor.getGeometryID();
            StringBuilder stringBuilder = new StringBuilder();
            OperatorExportToWktLocal.exportToWkt(m_export_flags, geometry, stringBuilder);
            return stringBuilder.toString();
        }
        return null;
    }

    @Override
    public int getID() {
        return m_index;
    }
}
