package com.esri.core.geometry;

public class OperatorImportFromGeoJsonCursor extends MapGeometryCursor {
    StringCursor m_jsonStringCursor;
//    String m_geoJsonString;
    int m_import_flags;
    int m_index;
    int m_count;

    // TODO, this was never implemented. Maybe remove?
//    public OperatorImportFromGeoJsonCursor(int import_flags, String geoJsonString, ProgressTracker progressTracker) {
//        m_geoJsonString = geoJsonString;
//        m_import_flags = import_flags;
//        m_index = -1;
//        m_count = 1;
//    }

    public OperatorImportFromGeoJsonCursor(int import_flags, StringCursor stringCursor, ProgressTracker progressTracker) {
        m_jsonStringCursor = stringCursor;
        m_import_flags = import_flags;
        m_index = -1;
        m_count = 1;
    }

    @Override
    public MapGeometry next() {
        String nextString;
        if ((nextString = m_jsonStringCursor.next()) != null) {
            m_index = m_jsonStringCursor.getID();
            JsonReader jsonReader = JsonParserReader.createFromString(nextString);
            return OperatorImportFromGeoJsonLocal.OperatorImportFromGeoJsonHelper.importFromGeoJson(m_import_flags, Geometry.Type.Unknown, jsonReader, null, false);
        }
        return null;
    }



    @Override
    public int getGeometryID() {
        return m_index;
    }
}
