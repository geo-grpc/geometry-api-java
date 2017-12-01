package com.esri.core.geometry;

public class OperatorImportFromWktCursor extends GeometryCursor {
    StringCursor m_wktStringCursor;
    int m_import_flags;
    int m_index;

    public OperatorImportFromWktCursor(int import_flags, StringCursor wkt_stringCursor) {
        m_index = -1;
        if (wkt_stringCursor == null)
            throw new GeometryException("invalid argument");

        m_import_flags = import_flags;
        m_wktStringCursor = wkt_stringCursor;
    }

    @Override
    public Geometry next() {
        String wktString = m_wktStringCursor.next();
        if (wktString != null) {
            m_index = m_wktStringCursor.getID();
            return OperatorImportFromWkt.local().execute(m_import_flags, Geometry.Type.Unknown, wktString, null);
        }
        return null;
    }

    @Override
    public int getGeometryID() {
        return m_index;
    }
}
