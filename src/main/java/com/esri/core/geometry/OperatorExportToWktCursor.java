package com.esri.core.geometry;

public class OperatorExportToWktCursor extends StringCursor {
    GeometryCursor m_geometryCursor;
    int m_export_flags;

    public OperatorExportToWktCursor(int exportFlags, GeometryCursor geometryCursor, ProgressTracker progressTracker) {
        if (geometryCursor == null)
            throw new GeometryException("invalid argument");

        m_export_flags = exportFlags;
        m_geometryCursor = geometryCursor;
    }

    @Override
    public String next() {
        Geometry geometry = m_geometryCursor.next();
        if (geometry != null) {
            StringBuilder stringBuilder = new StringBuilder();
            OperatorExportToWktLocal.exportToWkt(m_export_flags, geometry, stringBuilder);
            return stringBuilder.toString();
        }
        return null;
    }

    @Override
    public boolean hasNext() { return m_geometryCursor != null && m_geometryCursor.hasNext(); }

    @Override
    public long getID() {
        return m_geometryCursor.getGeometryID();
    }
}
