package com.esri.core.geometry;

class OperatorImportFromGeoJsonCursor extends MapGeometryCursor {
    StringCursor m_jsonStringCursor;
    String m_geoJsonString;
    int m_import_flags;
    int m_index;
    int m_count;

    public OperatorImportFromGeoJsonCursor(int import_flags, String geoJsonString, ProgressTracker progressTracker) {
        m_geoJsonString = geoJsonString;
        m_import_flags = import_flags;
        m_index = -1;
        m_count = 1;
    }

    public OperatorImportFromGeoJsonCursor(int import_flags, StringCursor stringCursor, ProgressTracker progressTracker) {
//        m_geoJsonString = geoJsonString;
//        m_import_flags = import_flags;
//        m_index = -1;
//        m_c
    }

    @Override
    public MapGeometry next() {
        return null;
    }

    @Override
    public int getGeometryID() {
        return m_index;
    }
}
