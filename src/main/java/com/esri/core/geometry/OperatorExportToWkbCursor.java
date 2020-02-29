package com.esri.core.geometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OperatorExportToWkbCursor extends ByteBufferCursor {
    private GeometryCursor m_geometryCursor;
    private int m_exportFlags;
    private SimpleStateEnum simpleStateEnum = SimpleStateEnum.SIMPLE_UNKNOWN;

    public OperatorExportToWkbCursor(int exportFlags, GeometryCursor geometryCursor) {
        if (geometryCursor == null)
            throw new GeometryException("invalid argument");

        m_exportFlags = exportFlags;
        m_geometryCursor = geometryCursor;
    }

    @Override
    public boolean hasNext() {
        return m_geometryCursor != null && m_geometryCursor.hasNext();
    }

    @Override
    public ByteBuffer next() {
        Geometry geometry;
        if (hasNext()) {
            geometry = preProject(m_geometryCursor);
            simpleStateEnum = geometry.getSimpleState();
            int size = OperatorExportToWkbLocal.exportToWKB(m_exportFlags, geometry, null);
            ByteBuffer wkbBuffer = ByteBuffer.allocate(size).order(ByteOrder.nativeOrder());
            OperatorExportToWkbLocal.exportToWKB(m_exportFlags, geometry, wkbBuffer);
            return wkbBuffer;
        }
        return null;
    }

    @Override
    public long getByteBufferID() {
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
}
