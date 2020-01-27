package com.esri.core.geometry;

public class OperatorImportFromMGRSCursor extends GeometryCursor {
    private StringCursor m_mgrsStringCursor;

    public OperatorImportFromMGRSCursor(SimpleStringCursor stringCursor) {
        m_mgrsStringCursor = stringCursor;
    }

    @Override
    public boolean hasNext() { return m_mgrsStringCursor != null && m_mgrsStringCursor.hasNext(); }

    @Override
    public Geometry next() {
        if (hasNext()) {
            String mgrsCode = m_mgrsStringCursor.next();
            Geometry geometry = OperatorImportFromMGRS.local().execute(Geometry.Type.Unknown, mgrsCode, null);
            SpatialReference outputSR = MGRS.getMGRSSpatialReference(mgrsCode);
            setResultSR(outputSR, true);
            postProject(geometry);
        }
        return null;
    }

    @Override
    public long getGeometryID() {
        return m_mgrsStringCursor.getID();
    }

    @Override
    public SimpleStateEnum getSimpleState() { return m_mgrsStringCursor.getSimpleState(); }

    @Override
    public String getFeatureID() { return m_mgrsStringCursor.getFeatureID(); }
}
